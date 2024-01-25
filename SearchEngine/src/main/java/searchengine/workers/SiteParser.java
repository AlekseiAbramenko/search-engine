package searchengine.workers;

import jakarta.transaction.Transactional;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import searchengine.config.Connection;
import searchengine.config.Repositories;
import searchengine.dto.indexing.PageParameters;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;

import searchengine.model.SiteModel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class SiteParser extends RecursiveAction {
    private List<SiteParser> taskList;
    private PageParameters pageParameters;
    private CopyOnWriteArraySet<String> linksList;
    private final Repositories repositories;
    private final Logger logger = LoggerFactory.getLogger(SiteParser.class);

    public SiteParser(PageParameters pageParameters,
                      CopyOnWriteArraySet<String> linksList, Repositories repositories) {
        this.pageParameters = pageParameters;
        this.linksList = linksList;
        this.repositories = repositories;
    }

    public SiteParser(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public void compute() {
        if (getPool().isShutdown() && taskList != null) {
            taskList.clear();
        } else {
            try {
                String url = pageParameters.getUrl();
                SiteModel siteModel = pageParameters.getSiteModel();
                taskList = new ArrayList<>();
                Document doc = Connection.getConnection(url);
                Thread.sleep(500);
                int cod = doc.connection().response().statusCode();
                String content = doc.html();
                String path = getPath(url, siteModel.getUrl());
                PageParameters pageParam = new PageParameters(siteModel, path, content, cod);
                if (repositories.getPageRepository().findPage(path).isEmpty()) {
                    postPageAndLemmas(pageParam);
                }
                Elements elements = doc.select("a[abs:href^=" + url + "]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link)) {
                        linksList.add(link);
                        PageParameters newPageParam = new PageParameters(siteModel, link);
                        SiteParser task = new SiteParser(newPageParam, linksList, repositories);
                        task.fork();
                        taskList.add(task);
                    }
                });
                taskList.forEach(ForkJoinTask::join);
            } catch (InterruptedException | IOException exception) {
                logger.error("Операция прервана или время соединения истекло!");
            }
        }
    }
    public synchronized void postPageAndLemmas(PageParameters pageParam) {
        int cod = pageParam.getCod();
        SiteModel siteModel = pageParam.getSiteModel();
        String content = pageParam.getContent();
        String path = pageParam.getUrl();
        postPage(pageParam);
        if (cod < 399) {
            addLemmasToDB(content, siteModel, path);
        }
    }
    public String getPath(String url, String siteUrl) {
        String path;
        if (url.equals(siteUrl)) {
            path = "/";
        } else {
            path = url.replace(siteUrl, "");
        }
        return path;
    }
    private boolean checkLink(String url) {
        return !linksList.contains(url)
                && !url.contains("#")
                && !url.contains(".pdf")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".php");
    }
    @Transactional
    void postPage(PageParameters pageParam) {
        SiteModel siteModel = pageParam.getSiteModel();
        String path = pageParam.getUrl();
        String content = pageParam.getContent();
        int cod = pageParam.getCod();
        Page page = new Page();
        page.setSite(siteModel);
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        repositories.getPageRepository().save(page);
        siteModel.setStatusTime(LocalDateTime.now());
        repositories.getSiteRepository().save(siteModel);
    }
    @Transactional
    synchronized void addLemmasToDB(String content, SiteModel siteModel, String path) {
        LemmasParser lemmasParser = new LemmasParser();
        try {
            HashMap<String, Integer> lemmas = lemmasParser.countLemmasFromText(content);
            lemmas.forEach((key, value) -> {
                try {
                    Optional<Lemma> optionalLemma =
                            repositories.getLemmaRepository().findLemma(key, siteModel);
                    if (optionalLemma.isPresent()) {
                        increaseLemmasFrequency(optionalLemma.get());
                    } else {
                        postLemma(key, siteModel);
                    }
                    Optional<Page> optionalPage = repositories.getPageRepository().findPage(path);
                    if (optionalPage.isPresent() && optionalLemma.isPresent()) {
                        Page page = optionalPage.get();
                        Lemma lemma = optionalLemma.get();
                        if (repositories.getIndexRepository().existsIndex(page, lemma)) {
                            repositories.getIndexRepository().updateIndex(value, page, lemma);
                        } else {
                            postIndex(page, lemma, value);
                        }
                    }
                } catch (IncorrectResultSizeDataAccessException ex) {
                    logger.error("дублирование: " + "lemma: " + key + "; site: " + siteModel.getId());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    private void postIndex(Page page, Lemma lemma, int rank) {
        IndexModel index = new IndexModel();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRank(rank);
        repositories.getIndexRepository().save(index);
    }
    @Transactional
    synchronized void postLemma(String name, SiteModel siteModel) {
        if (repositories.getLemmaRepository().findLemma(name, siteModel).isPresent()) {
            logger.error("Попытка повторно добавить лемму " + name + " " + siteModel.getId());
        } else {
            Lemma lemma = new Lemma();
            lemma.setLemma(name);
            lemma.setFrequency(1);
            lemma.setSite(siteModel);
            repositories.getLemmaRepository().save(lemma);
        }
    }
    @Transactional
    private void increaseLemmasFrequency(Lemma lemma) {
        String name = lemma.getLemma();
        SiteModel siteModel = lemma.getSite();
        int newFrequency = lemma.getFrequency() + 1;
        repositories.getLemmaRepository().updateLemmasFrequency(newFrequency, name, siteModel);
    }
}