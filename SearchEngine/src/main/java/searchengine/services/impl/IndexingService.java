package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.Lemmatizator;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteParcer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingService implements searchengine.services.IndexingService {
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final SitesList sites;
    private ExecutorService service;
    private final List<ForkJoinPool> poolsList = new ArrayList<>();

    //todo: настроить зависимости (при удалении сайта, удалять его страницы)

    //todo: теперь не работает индексация, из-за того, что я удаляю сайт и страницу,
    // а они завязаны на индексе и леммах. Исправить это! Лучше, если через настройку зависимостей!!!

    //todo: встроить код с индексации одной страницы в код getIndexing

    //todo: написать тесты для разных частей проложения

    @Override
    @Transactional
    public void getIndexing() {
        service = Executors.newFixedThreadPool(sites.getSites().size());
        for (Site site : sites.getSites()) {
            service.submit(() -> {
                String url = site.getUrl();
                String name = site.getName();
                if (siteRepository.findSiteByUrl(url).isPresent()) {
                    pageRepository.deletePagesBySiteId(getSiteModelFromDB(url));
                    siteRepository.deleteSiteByUrl(url);
                }
                postSite(name, url);
                SiteModel siteModel = getSiteModelFromDB(url);
                CopyOnWriteArraySet<String> linksList = new CopyOnWriteArraySet<>();
                ForkJoinPool pool = new ForkJoinPool();
                poolsList.add(pool);
                pool.invoke(new SiteParcer(
                        siteModel, url, linksList, pageRepository, siteRepository));
                if (pool.isShutdown() && siteModel.getStatus().equals(SiteStatus.INDEXING)) {
                    setFailedStatus(siteModel);
                } else {
                    setIndexedStatus(siteModel);
                }
            });
        }
        service.shutdown();
    }

    @Transactional
    @Override
    public void stopIndexing() {
        poolsList.forEach(ForkJoinPool::shutdownNow);
        service.shutdownNow();
        poolsList.clear();
    }

    @Transactional
    @Override
    public void indexingPage(String link) {
        String siteUrl = "";
        String siteName = "";
        for(Site site : sites.getSites()) {
            String substring = link.substring(0, site.getUrl().length());
            if(substring.equals(site.getUrl())) {
                siteUrl = site.getUrl();
                siteName = site.getName();
                break;
            }
        }

        Document doc = getConnection(link);
        Integer cod = doc.connection().response().statusCode();
        String content = doc.html();
        String path = link.replace(siteUrl, "/");

        if(pageRepository.existPage(path)) {
            cleanDB(path);
        }

        if (siteRepository.findSiteByUrl(siteUrl).isEmpty()) {
            postSite(siteName, siteUrl);
        }

        SiteModel siteModel = siteRepository.findSiteByUrl(siteUrl).get();
        postPage(path, cod, content, siteModel);
        addLemmasToDB(content, siteModel, path);
    }

    private void cleanDB(String path) {
        Page page = pageRepository.findPage(path).get();
        List<IndexModel> indexList = indexRepository.findLemmasByPage(page);
        indexList.forEach(index -> {
            Lemma lemma = index.getLemma();
            if (lemma.getFrequency() > 1) {
                String name = lemma.getLemma();
                int newFrequency = lemma.getFrequency() - 1;
                lemmaRepository.updateLemmasFrequency(newFrequency, name);
            } else {
                lemmaRepository.delete(lemma);
            }
        });
        indexRepository.deleteIndexByPage(page);
        pageRepository.delete(page);
    }

    private void addLemmasToDB(String content, SiteModel siteModel, String path) {
        Lemmatizator lemmatizator = new Lemmatizator();
        try {
            Map<String, Integer> lemmas = lemmatizator.countLemmasFromText(content);
            lemmas.forEach((key, value) -> {
                if(lemmaRepository.findLemmaByName(key).isPresent()) {
                    increaseLemmasFrequency(key);
                } else {
                    postLemma(key, siteModel);
                }
                if(pageRepository.findPage(path).isPresent()
                        && lemmaRepository.findLemmaByName(key).isPresent()) {
                    Page page = pageRepository.findPage(path).get();
                    Lemma lemma = lemmaRepository.findLemmaByName(key).get();
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
        setIndexedStatus(siteModel);
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
    private void increaseLemmasFrequency(String name) {
        Lemma lemma = lemmaRepository.findLemmaByName(name).get();
        int newFrequency = lemma.getFrequency() + 1;
        lemmaRepository.updateLemmasFrequency(newFrequency, lemma.getLemma());
    }

    @Transactional
    public void postPage(String path, Integer cod, String content, SiteModel siteModel) {
        Page page = new Page();
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        page.setSite(siteModel);
        pageRepository.save(page);
    }

    @Transactional
    public void postSite(String name, String url) {
        SiteModel siteModel = new SiteModel();
        siteModel.setName(name);
        siteModel.setUrl(url);
        siteModel.setStatus(SiteStatus.INDEXING);
        siteModel.setLastError("");
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    public SiteModel getSiteModelFromDB(String url) {
        Optional<SiteModel> siteModel = siteRepository.findSiteByUrl(url);
        return siteModel.orElse(null);
    }

    @Transactional
    public void setIndexedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.INDEXED);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    @Transactional
    public void setFailedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.FAILED);
        siteModel.setLastError("Индексация прервана пользователем");
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    public ExecutorService getService() {
        return service;
    }

    @Override
    public boolean checkLink(String link) {
        boolean pageIsOk = false;
            for (Site site : sites.getSites()) {
                String substring = link.substring(0, site.getUrl().length());
                if (substring.equals(site.getUrl())) {
                    pageIsOk = true;
                    break;
                }
            }
        return pageIsOk
                && !link.contains("#")
                && !link.contains(".pdf")
                && !link.contains(".png")
                && !link.contains(".jpg")
                && !link.contains(".php");
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
