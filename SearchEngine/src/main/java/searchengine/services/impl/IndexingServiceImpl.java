package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.Connection;
import searchengine.config.Repositories;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.LocalDB;
import searchengine.dto.indexing.PageParameters;
import searchengine.model.*;
import searchengine.workers.SiteParser;
import searchengine.workers.Worker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements searchengine.services.IndexingService {
    private final SitesList sites;
    private final Repositories repositories;
    @Getter
    private ExecutorService siteParserService;
    private ForkJoinPool siteParserPool;
    @Getter
    private ForkJoinPool lemmasAndIndexesParserPool; //todo: добавть в контроллер
    private SiteModel siteModel;
    private final Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Override
    public void getIndexing() {//todo: ЗАКОМИТИТЬ!
        siteParserService = Executors.newSingleThreadExecutor();
        for (Site site : sites.getSites()) {
            siteParserService.submit(() -> {
                String url = site.getUrl();
                String name = site.getName();
                if (repositories.getSiteRepository().findSiteByUrl(url).isPresent()) {
                    cleanDB(url);
                }
                postSite(name, url);
                siteModel = getSiteModelFromDB(url);
                CopyOnWriteArraySet<String> linksSet = new CopyOnWriteArraySet<>();
                CopyOnWriteArraySet<Page> pagesSet = new CopyOnWriteArraySet<>();
                PageParameters pageParameters = new PageParameters(siteModel, url);
                siteParserPool = new ForkJoinPool();
                //todo: почему первая 3 раза добавляется??? может они разные?? проверить!
                // ее вручную в сервисе? уже с ней присылать linksSet!
                List<Page> pagesList = siteParserPool.invoke(new SiteParser(pageParameters, linksSet, pagesSet));
//todo: logger.info                System.out.println("PagesList is done. Size: " + pagesList.size());
                pagesList.forEach(page -> repositories.getPageRepository().save(page));
                LocalDB localDB = addLemmasAndIndexes(pagesList, siteModel);
//todo: logger.info                System.out.println(STR."LocalDB is done. LemmasList size: \{localDB.getLemmasList().size()} IndexesList size: \{localDB.getIndexesSet().size()}");
                localDB.getLemmasList().forEach(lemma -> repositories.getLemmaRepository().save(lemma));
                localDB.getIndexesSet().forEach(indexModel -> repositories.getIndexRepository().save(indexModel));
                setIndexedStatus(siteModel);
            });
        }
        siteParserService.shutdown();
    }

    private LocalDB addLemmasAndIndexes(List<Page> pagesList, SiteModel siteModel){
        CopyOnWriteArraySet<IndexModel> indexesSet = new CopyOnWriteArraySet<>();
        ConcurrentHashMap<String, Lemma> lemmasMap = new ConcurrentHashMap<>();
        lemmasAndIndexesParserPool = new ForkJoinPool();
        return lemmasAndIndexesParserPool.invoke(new Worker(siteModel, pagesList, indexesSet, lemmasMap));
    }

    @Override
    public void stopIndexing() {
        siteParserPool.shutdownNow();
        siteParserService.shutdownNow();
        if (lemmasAndIndexesParserPool != null ) {
            lemmasAndIndexesParserPool.shutdownNow();
        }
        setFailedStatus(siteModel);
    }

    @Override
    public void indexingPage(String link) {//todo: переделать!
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
            SiteParser siteParser = new SiteParser();
            String path = siteParser.getPath(link, siteUrl);
            int code = doc.connection().response().statusCode();
            String content = doc.html();
            checkPageAndRemove(path);
            SiteModel siteModel = checkSite(siteName, siteUrl);
            PageParameters pageParameters = new PageParameters(siteModel, path, content, code);
            postPage(pageParameters);
            Page page = repositories.getPageRepository().findPage(path).get();
//            new LemmaAdder(page, repositories).run();
        } catch (IOException exception) {
            logger.error("Время соединения истекло");
        }
    }

    private SiteModel checkSite(String siteName, String siteUrl) {
        SiteModel siteModel = getSiteModelFromDB(siteUrl);
        if (siteModel == null) {
            postSite(siteName, siteUrl);
            return getSiteModelFromDB(siteUrl);
        } else {
            return siteModel;
        }
    }

    @Transactional
    private void checkPageAndRemove(String path) {
        Optional<Page> optionalPage = repositories.getPageRepository().findPage(path);
        if (optionalPage.isPresent()) {
            Page page = optionalPage.get();
            List<IndexModel> indexList = repositories.getIndexRepository().findIndexesByPage(page);
            indexList.forEach(index -> {
                Lemma lemma = index.getLemma();
                if (lemma.getFrequency() > 1) {
                    decreaseLemmasFrequency(lemma);
                } else { //todo: разобраться, в чем вопрос! здесь эксепшн вылетает
                    System.out.println(lemma.getId());
                    repositories.getIndexRepository().delete(index);
                    repositories.getLemmaRepository().delete(lemma);
                }
            });
            repositories.getIndexRepository().deleteIndexesByPage(page);
            repositories.getPageRepository().delete(page);
        }
    }

    @Transactional
    private void decreaseLemmasFrequency(Lemma lemma) {
        int newFrequency = lemma.getFrequency() - 1;
        repositories.getLemmaRepository().updateLemmasFrequency(newFrequency, lemma);
    }

    @Transactional
    private void cleanDB(String url) {
        SiteModel siteModel = getSiteModelFromDB(url);
        List<Page> pagesList = repositories.getPageRepository().findPagesBySite(siteModel);
        pagesList.forEach(page -> {
            repositories.getIndexRepository().deleteIndexesByPage(page);
        });
        repositories.getLemmaRepository().deleteLemmasBySite(siteModel);
        repositories.getPageRepository().deletePagesBySite(siteModel);
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

    @Transactional
    void postPage(PageParameters pageParam) {
        Page page = new Page();
        SiteModel siteModel = pageParam.getSiteModel();
        page.setSite(siteModel);
        page.setPath(pageParam.getUrl());
        page.setCode(pageParam.getCod());
        page.setContent(pageParam.getContent());
        repositories.getPageRepository().save(page);
    }

    private SiteModel getSiteModelFromDB(String url) {
        Optional<SiteModel> siteModel = repositories.getSiteRepository().findSiteByUrl(url);
        return siteModel.orElse(null);
    }

    @Transactional
    private void setIndexedStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.INDEXED);
        siteModel.setLastError("");
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
