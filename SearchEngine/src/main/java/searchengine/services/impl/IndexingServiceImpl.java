package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Connection;
import searchengine.config.Repositories;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.PageParameters;
import searchengine.model.*;
import searchengine.workers.SiteParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements searchengine.services.IndexingService {
    @Autowired
    private Repositories repositories;
    private final SitesList sites;
    @Getter
    private ExecutorService service;
    private ForkJoinPool pool;
    private final Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Override
    public void getIndexing() {
        service = Executors.newSingleThreadExecutor();
        for (Site site : sites.getSites()) {
            service.submit(() -> {
                String url = site.getUrl();
                String name = site.getName();
                if (repositories.getSiteRepository().findSiteByUrl(url).isPresent()) {
                    cleanDB(url);
                }
                postSite(name, url);
                SiteModel siteModel = getSiteModelFromDB(url);
                CopyOnWriteArraySet<String> linksList = new CopyOnWriteArraySet<>();
                PageParameters pageParameters = new PageParameters(siteModel, url);
                pool = new ForkJoinPool();
                pool.invoke(new SiteParser(pageParameters, linksList, repositories));
                if (pool.isShutdown() && siteModel.getStatus().equals(SiteStatus.INDEXING)) {
                    setFailedStatus(siteModel);
                } else {
                    setIndexedStatus(siteModel);
                }
            });
        }
        service.shutdown();
    }
    @Override
    public void stopIndexing() {
        pool.shutdownNow();
        service.shutdownNow();
    }
    @Override
    public void indexingPage(String link) {
        String siteUrl = "";
        String siteName = "";
        for (Site site : sites.getSites()) {
            String substring = link.substring(0, site.getUrl().length());
            if (substring.equals(site.getUrl())) {
                siteUrl = site.getUrl();
                siteName = site.getName();
                break;
            }
        }
        try {
            Document doc = Connection.getConnection(link);
            SiteParser siteParser = new SiteParser(repositories);
            int cod = doc.connection().response().statusCode();
            String content = doc.html();
            String path = siteParser.getPath(link, siteUrl);
            checkPageAndRemove(path);
            SiteModel siteModel = checkSite(siteName, siteUrl);
            PageParameters pageParam = new PageParameters(siteModel, path, content, cod);
            siteParser.postPageAndLemmas(pageParam);
            setIndexedStatus(siteModel);
        } catch (IOException exception) {
            logger.error("Время соединения истекло");
        }
    }
    private SiteModel checkSite(String siteName, String siteUrl) {
       SiteModel siteModel = getSiteModelFromDB(siteUrl);
        if (siteModel == null) {
            postSite(siteName, siteUrl);
        }
        return siteModel;
    }
    private void checkPageAndRemove(String path) {
        Optional<Page> optionalPage = repositories.getPageRepository().findPage(path);
        if (optionalPage.isPresent()) {
            Page page = optionalPage.get();
            removePageInformation(page);
        }
    }
    private void removePageInformation(Page page) {
        List<IndexModel> indexList = repositories.getIndexRepository().findLemmasByPage(page);
        repositories.getIndexRepository().deleteIndexByPage(page);
        indexList.forEach(index -> {
            Lemma lemma = index.getLemma();
            if (lemma.getFrequency() > 1) {
                String name = lemma.getLemma();
                SiteModel siteModel = lemma.getSite();
                int newFrequency = lemma.getFrequency() - 1;
                repositories.getLemmaRepository().updateLemmasFrequency(newFrequency, name, siteModel);
            } else {
                repositories.getLemmaRepository().delete(lemma);
            }
        });
        repositories.getPageRepository().delete(page);
    }
    private void cleanDB(String url) {
        SiteModel siteModel = getSiteModelFromDB(url);
        List<Page> pagesList = repositories.getPageRepository().findPagesBySite(siteModel);
        pagesList.forEach(page -> {
            repositories.getIndexRepository().deleteIndexByPage(page);
        });
        repositories.getPageRepository().deletePagesBySite(siteModel);;
        repositories.getLemmaRepository().deleteLemmasBySite(siteModel);
        repositories.getSiteRepository().delete(siteModel);
    }
    @Transactional
    private void postSite(String name, String url) {
        SiteModel siteModel = new SiteModel();
        siteModel.setName(name);
        siteModel.setUrl(url);
        siteModel.setStatus(SiteStatus.INDEXING);
        siteModel.setLastError("");
        siteModel.setStatusTime(LocalDateTime.now());
        repositories.getSiteRepository().save(siteModel);
    }
    private SiteModel getSiteModelFromDB(String url) {
        Optional<SiteModel> siteModel = repositories.getSiteRepository().findSiteByUrl(url);
        return siteModel.orElse(null);
    }
    @Transactional
    private void setIndexedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.INDEXED);
        siteModel.setStatusTime(LocalDateTime.now());
        repositories.getSiteRepository().save(siteModel);
    }
    @Transactional
    private void setFailedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.FAILED);
        siteModel.setLastError("Индексация прервана пользователем");
        siteModel.setStatusTime(LocalDateTime.now());
        repositories.getSiteRepository().save(siteModel);
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
}
