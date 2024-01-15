package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Connection;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ParsingParameters;
import searchengine.model.*;
import searchengine.repository.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingService implements searchengine.services.IndexingService {
    @Autowired
    private Repositories repositories;
    private final SitesList sites;
    @Getter
    private ExecutorService service;
    private ForkJoinPool pool;

    @Override
    @Transactional
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
                ParsingParameters parsingParameters = new ParsingParameters(siteModel, url);
                pool = new ForkJoinPool();
                pool.invoke(new SiteParcer(parsingParameters, linksList, repositories));
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
        pool.shutdownNow();
        service.shutdownNow();
    }

    @Transactional
    @Override
    public void indexingPage(String link) {//todo: сократить метод
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
            int cod = doc.connection().response().statusCode();
            String content = doc.html();
            String path;

            if (link.equals(siteUrl)) {
                path = "/";
            } else {
                path = link.replace(siteUrl, "");
            }

            Optional<Page> optionalPage = repositories.getPageRepository().findPage(path);
            if (optionalPage.isPresent()) {
                Page page = optionalPage.get();
                removePageInformation(page);
            }

            Optional<SiteModel> optionalSiteModel = repositories.getSiteRepository().findSiteByUrl(siteUrl);
            if (optionalSiteModel.isEmpty()) {
                postSite(siteName, siteUrl);
            }

            SiteModel siteModel = optionalSiteModel.get();
            SiteParcer siteParcer = new SiteParcer(repositories);
            siteParcer.postPage(cod, content, path, siteModel);
            if (cod < 399) {
                siteParcer.addLemmasToDB(content, siteModel, path);
            }
            setIndexedStatus(siteModel);
        } catch (IOException exception) {
            System.out.println("Время соединения истекло");
//            logger.error("Время соединения истекло");
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
        List<Page> pageList = repositories.getPageRepository().findPagesBySite(siteModel);
        pageList.forEach(page -> {
            repositories.getIndexRepository().deleteIndexByPage(page);
            repositories.getPageRepository().delete(page);
        });
        repositories.getLemmaRepository().deleteLemmasBySite(siteModel);
        repositories.getSiteRepository().deleteSiteByUrl(url);
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
