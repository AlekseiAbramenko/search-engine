package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

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
    @Getter
    private ExecutorService service;
    private final List<ForkJoinPool> poolsList = new ArrayList<>();

    @Override
    @Transactional
    public void getIndexing() {
        service = Executors.newFixedThreadPool(sites.getSites().size());
        for (Site site : sites.getSites()) {
            service.submit(() -> {
                String url = site.getUrl();
                String name = site.getName();
                if (siteRepository.findSiteByUrl(url).isPresent()) {
                    cleanDB(url);
                }
                postSite(name, url);
                SiteModel siteModel = getSiteModelFromDB(url);
                CopyOnWriteArraySet<String> linksList = new CopyOnWriteArraySet<>();
                ForkJoinPool pool = new ForkJoinPool();
                poolsList.add(pool);
                pool.invoke(new SiteParcer(
                        siteModel, url, linksList, pageRepository, siteRepository,
                        lemmaRepository, indexRepository));
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
        int cod = doc.connection().response().statusCode();
        String content = doc.html();
        String path;

        if(link.equals(siteUrl)) {
            path = "/";
        } else {
            path = link.replace(siteUrl, "");
        }

        if(pageRepository.findPage(path).isPresent()) {
            Page page = pageRepository.findPage(path).get();
            removePageInformation(page);
        }

        if (siteRepository.findSiteByUrl(siteUrl).isEmpty()) {
            postSite(siteName, siteUrl);
        }

        SiteModel siteModel = siteRepository.findSiteByUrl(siteUrl).get();
        postPage(path, cod, content, siteModel);
        if(cod<399) {
            addLemmasToDB(content, siteModel, path);
        }
    }

    private void removePageInformation(Page page) {
        List<IndexModel> indexList = indexRepository.findLemmasByPage(page);
        indexRepository.deleteIndexByPage(page);
        indexList.forEach(index -> {
            Lemma lemma = index.getLemma();
            if (lemma.getFrequency() > 1) {
                String name = lemma.getLemma();
                SiteModel siteModel = lemma.getSite();
                int newFrequency = lemma.getFrequency() - 1;
                lemmaRepository.updateLemmasFrequency(newFrequency, name, siteModel);
            } else {
                lemmaRepository.delete(lemma);
            }
        });
        pageRepository.delete(page);
    }

    private void cleanDB(String url) {
        SiteModel siteModel = getSiteModelFromDB(url);
        List<Page> pageList = pageRepository.findPagesBySite(siteModel);
        pageList.forEach(page -> {
            indexRepository.deleteIndexByPage(page);
            pageRepository.delete(page);
        });
        lemmaRepository.deleteLemmasBySite(siteModel);
        siteRepository.deleteSiteByUrl(url);
    }

    private void addLemmasToDB(String content, SiteModel siteModel, String path) {
        //todo: этот метод дублируется, от него нужно избавиться! Сделать из него класс, добавить туда сопутствующие методы, которые также дублируются!
        LemmasParcer lemmasParcer = new LemmasParcer();
        try {
            Map<String, Integer> lemmas = lemmasParcer.countLemmasFromText(content);
            lemmas.forEach((key, value) -> {
                Optional<Lemma> optionalLemma = lemmaRepository.findLemma(key, siteModel);
                if(optionalLemma.isPresent()) {
                    increaseLemmasFrequency(optionalLemma.get());
                } else {
                    postLemma(key, siteModel);
                }
                if(pageRepository.findPage(path).isPresent()
                        && optionalLemma.isPresent()) {
                    Page page = pageRepository.findPage(path).get();
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
    private void increaseLemmasFrequency(Lemma lemma) {
        String name = lemma.getLemma();
        SiteModel siteModel = lemma.getSite();
        int newFrequency = lemma.getFrequency() + 1;
        lemmaRepository.updateLemmasFrequency(newFrequency, name, siteModel);
    }

    @Transactional
    private void postPage(String path, Integer cod, String content, SiteModel siteModel) {
        Page page = new Page();
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        page.setSite(siteModel);
        pageRepository.save(page);
    }

    @Transactional
    private void postSite(String name, String url) {
        SiteModel siteModel = new SiteModel();
        siteModel.setName(name);
        siteModel.setUrl(url);
        siteModel.setStatus(SiteStatus.INDEXING);
        siteModel.setLastError("");
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    private SiteModel getSiteModelFromDB(String url) {
        Optional<SiteModel> siteModel = siteRepository.findSiteByUrl(url);
        return siteModel.orElse(null);
    }

    @Transactional
    private void setIndexedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.INDEXED);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    @Transactional
    private void setFailedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.FAILED);
        siteModel.setLastError("Индексация прервана пользователем");
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
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

    private Document getConnection(String link) {
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
