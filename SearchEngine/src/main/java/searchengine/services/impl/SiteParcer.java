package searchengine.services.impl;

import jakarta.transaction.Transactional;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import searchengine.config.Connection;
import searchengine.dto.indexing.ParsingParameters;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;

import searchengine.model.SiteModel;
import searchengine.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class SiteParcer extends RecursiveAction {
    private List<SiteParcer> taskList;
    private ParsingParameters parsingParameters;
    private CopyOnWriteArraySet<String> linksList;
    private final Repositories repositories;
    private final Logger logger = LoggerFactory.getLogger(SiteParcer.class);

    public SiteParcer(ParsingParameters parsingParameters,
                      CopyOnWriteArraySet<String> linksList,  Repositories repositories) {
        this.parsingParameters = parsingParameters;
        this.linksList = linksList;
        this.repositories = repositories;
    }

    public SiteParcer(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public void compute() { //todo: сократить метод!
        if(getPool().isShutdown() && taskList != null) {
            taskList.clear();
        } else {
            try {
                String url = parsingParameters.getUrl();
                SiteModel siteModel = parsingParameters.getSiteModel();
                taskList = new ArrayList<>();
                Document doc = Connection.getConnection(url);
                Thread.sleep(500);
                String path;
                if(url.equals(siteModel.getUrl())) {
                    path = "/";
                } else {
                    path = url.replace(siteModel.getUrl(), "");
                }
                int cod = doc.connection().response().statusCode();
                String content = doc.html();
                if(repositories.getPageRepository().findPage(path).isEmpty()) {
                    postPage(cod, content, path, siteModel);
                    if(cod<399) {
                        addLemmasToDB(content, siteModel, path);
                    }
                }
                Elements elements = doc.select("a[abs:href^=" + url + "]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link)) {
                        linksList.add(link);
                        ParsingParameters newParsingParameters = new ParsingParameters(siteModel, link);
                        SiteParcer task = new SiteParcer(newParsingParameters, linksList, repositories);
                        task.fork();
                        taskList.add(task);
                    }
                });
                taskList.forEach(ForkJoinTask::join);
            } catch (CancellationException exception) {
                logger.error("Операция отменена!");
            } catch (IOException | RuntimeException exception) {
                logger.error("Время соединения истекло!");
            } catch (InterruptedException exception) {
                logger.error("Операция прервана!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    void postPage(Integer cod, String content, String path, SiteModel siteModel) {
        Page page = new Page();
        page.setSite(siteModel);
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        repositories.getPageRepository().save(page);
        siteModel.setStatusTime(LocalDateTime.now());
        repositories.getSiteRepository().save(siteModel);
    }

    synchronized void addLemmasToDB(String content, SiteModel siteModel, String path) {
        LemmasParcer lemmasParcer = new LemmasParcer();
        try {
            HashMap<String, Integer> lemmas = lemmasParcer.countLemmasFromText(content);
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
                    logger.error("lemma: " + key + "; site: " + siteModel.getId());
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
        Lemma lemma = new Lemma();
        lemma.setLemma(name);
        lemma.setFrequency(1);
        lemma.setSite(siteModel);
        repositories.getLemmaRepository().save(lemma);
    }

    @Transactional
    private void increaseLemmasFrequency(Lemma lemma) {
        String name = lemma.getLemma();
        SiteModel siteModel = lemma.getSite();
        int newFrequency = lemma.getFrequency() + 1;
        repositories.getLemmaRepository().updateLemmasFrequency(newFrequency, name, siteModel);
    }
}