package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;

import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class SiteParcer extends RecursiveAction {
    private List<SiteParcer> taskList;
    private final SiteModel siteModel;
    private final String link;
    private final CopyOnWriteArraySet<String> linksList;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public void compute() {
        if(getPool().isShutdown()) {
            System.out.println(siteModel.getName());
            System.out.println("ЧТО-ТО ПОШЛО НЕ ТАК!!!");
            taskList.clear();
        } else {
            try {
                taskList = new ArrayList<>();
                Document doc = getConnection(link);
                Thread.sleep(500);

                String path;
                if(link.equals(siteModel.getUrl())) {
                    path = "/";
                } else {
                    path = link.replace(siteModel.getUrl(), "");
                }
                int cod = doc.connection().response().statusCode();
                String content = doc.html();
                if(pageRepository.findPage(path).isEmpty()) {
                    postPage(cod, content, path, siteModel);
                    if(cod<399) {
                        addLemmasToDB(content, siteModel, path);
                    }
                }
                Elements elements = doc.select("a[abs:href^=" + link + "]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link)) {
                        linksList.add(link);
                        SiteParcer task = new SiteParcer(
                                siteModel, link, linksList, pageRepository, siteRepository,
                                lemmaRepository, indexRepository);
                        task.fork();
                        taskList.add(task);
                    }
                });
                taskList.forEach(ForkJoinTask::join);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkLink(String url) {
        return !linksList.contains(url)
                && !url.contains("#")
                && !url.contains(".pdf")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".php");
    }

    @Transactional
    public void postPage(Integer cod, String content, String path, SiteModel siteModel) {
        Page page = new Page();
        page.setSite(siteModel);
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        pageRepository.save(page);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    private void addLemmasToDB(String content, SiteModel siteModel, String path) {
        LemmasParcer lemmasParcer = new LemmasParcer();
        try {
            HashMap<String, Integer> lemmas = lemmasParcer.countLemmasFromText(content);
            lemmas.forEach((key, value) -> {
                Optional<Lemma> optionalLemma = lemmaRepository.findLemma(key, siteModel);
                if(optionalLemma.isPresent()) {
                    increaseLemmasFrequency(optionalLemma.get());
                } else {
                    postLemma(key, siteModel);
                }
                Optional<Page> optionalPage = pageRepository.findPage(path);
                if(optionalPage.isPresent() && optionalLemma.isPresent()) {
                    Page page = optionalPage.get();
                    Lemma lemma = optionalLemma.get();
                    if (indexRepository.existsIndex(page, lemma)) {
                        indexRepository.updateIndex(value, page, lemma);
                    } else {
                        postIndex(page, lemma, value);
                    }
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
        indexRepository.save(index);
    }

    @Transactional
    private void postLemma(String name, SiteModel siteModel) {
        Lemma lemma = new Lemma();
        lemma.setLemma(name);
        lemma.setFrequency(1);
        lemma.setSite(siteModel);
        lemmaRepository.save(lemma);
    }

    @Transactional
    private void increaseLemmasFrequency(Lemma lemma) {
        String name = lemma.getLemma();
        SiteModel siteModel = lemma.getSite();
        int newFrequency = lemma.getFrequency() + 1;
        lemmaRepository.updateLemmasFrequency(newFrequency, name, siteModel);
    }

    public Document getConnection(String link) {
        try {
            return Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(10000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}