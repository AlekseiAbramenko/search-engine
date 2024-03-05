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
import searchengine.model.ParsingParameters;
import searchengine.model.*;
import searchengine.workers.LemmaParser;
import searchengine.workers.SiteParser;
import searchengine.workers.LemmaAndIndexCollector;

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
    private ForkJoinPool lemmasAndIndexesParserPool;
    private SiteModel siteModel;
    private final Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Override
    public void getIndexing() {
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
                ParsingParameters parsingParameters = new ParsingParameters(siteModel, url, linksSet, pagesSet);
                siteParserPool = new ForkJoinPool();
                List<Page> pagesList = siteParserPool.invoke(new SiteParser(parsingParameters, repositories));
                repositories.getPageRepository().saveAll(pagesList);
                LocalDB localDB = addLemmasAndIndexes(pagesList, siteModel);
                repositories.getLemmaRepository().saveAll(localDB.getLemmasList());
                repositories.getIndexRepository().saveAll(localDB.getIndexesSet());
                setIndexedStatus(siteModel);
            });
        }
        siteParserService.shutdown();
    }

    private LocalDB addLemmasAndIndexes(List<Page> pagesList, SiteModel siteModel) {
        CopyOnWriteArraySet<IndexModel> indexesSet = new CopyOnWriteArraySet<>();
        ConcurrentHashMap<String, Lemma> lemmasMap = new ConcurrentHashMap<>();
        lemmasAndIndexesParserPool = new ForkJoinPool();
        CollectorParameters collectorParameters = new CollectorParameters(siteModel, pagesList, indexesSet, lemmasMap);
        return lemmasAndIndexesParserPool.invoke(new LemmaAndIndexCollector(collectorParameters));
    }

    @Override
    public void stopIndexing() {
        siteParserPool.shutdownNow();
        siteParserService.shutdownNow();
        if (lemmasAndIndexesParserPool != null) {
            lemmasAndIndexesParserPool.shutdownNow();
        }
        setFailedStatus(siteModel);
    }

    @Transactional
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
            SiteParser siteParser = new SiteParser();
            String path = siteParser.getPath(link, siteUrl);
            int code = doc.connection().response().statusCode();
            String content = doc.html();
            checkPageAndRemove(path);
            siteModel = checkSite(siteName, siteUrl);
            repositories.getPageRepository().save(new Page(siteModel, path, code, content));
            repositories.getSiteRepository().updateStatusTime(LocalDateTime.now(), siteModel);
            postLemmasAndIndexes(repositories.getPageRepository().findPage(path).get());
        } catch (IOException exception) {
            logger.error("Время соединения истекло");
        }
    }

    @Transactional
    private void postLemmasAndIndexes(Page page) {
        LemmaParser lemmaParser = new LemmaParser();
        HashMap<String, Integer> lemmas;
        try {
            lemmas = lemmaParser.countLemmasFromText(page.getContent());
            lemmas.forEach((key, value) -> {
                List<Lemma> lemmasList = repositories.getLemmaRepository().findLemmasList(key, siteModel);
                if (!lemmasList.isEmpty()) {
                    Lemma lemmaFromDB = lemmasList.getFirst();
                    repositories.getLemmaRepository().
                            updateLemmasFrequency(lemmaFromDB.getFrequency() + 1, lemmaFromDB);
                    if (lemmasList.size() > 1) {
                        deleteLemmasDouble(lemmasList);
                    }
                } else {
                    repositories.getLemmaRepository().save(new Lemma(siteModel, key, 1));
                }
                List<Lemma> newLemmasList = repositories.getLemmaRepository().findLemmasList(key, siteModel);
                if (!lemmasList.isEmpty()) {
                    Lemma lemma = newLemmasList.getFirst();
                    if (repositories.getIndexRepository().existsIndex(page, lemma)) {
                        repositories.getIndexRepository().updateIndex(value, page, lemma);
                    } else {
                        repositories.getIndexRepository().save(new IndexModel(page, lemma, value));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    private void deleteLemmasDouble(List<Lemma> lemmasList) {
        for (int i = 1; i < lemmasList.size(); i++) {
            Lemma lemma = lemmasList.get(i);
            repositories.getIndexRepository().deleteIndexesByLemma(lemma);
            repositories.getLemmaRepository().delete(lemma);
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
                    repositories.getLemmaRepository().updateLemmasFrequency(lemma.getFrequency() - 1, lemma);
                } else {
                    repositories.getIndexRepository().delete(index);
                    repositories.getLemmaRepository().delete(lemma);
                }
            });
            repositories.getIndexRepository().deleteIndexesByPage(page);
            repositories.getPageRepository().delete(page);
        }
    }

    @Transactional
    private void cleanDB(String url) {
        SiteModel siteModel = getSiteModelFromDB(url);
        setIndexingStatus(siteModel);
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

    @Transactional
    private void setIndexingStatus(SiteModel siteModel) {
        siteModel.setStatus(SiteStatus.INDEXING);
        siteModel.setLastError("");
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
