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
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.LocalDB;
import searchengine.dto.search.CollectorParameters;
import searchengine.dto.indexing.ParsingParameters;
import searchengine.model.*;
import searchengine.utils.LemmaParser;
import searchengine.utils.SiteParser;
import searchengine.utils.LemmaAndIndexCollector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements searchengine.services.IndexingService {
    private final SitesList sites;
    private final Repositories repositories;
    @Getter
    private ExecutorService siteParserService;
    private ForkJoinPool siteParserPool;
    private ForkJoinPool lemmasAndIndexesParserPool;
    private SiteModel siteModel;
    private final Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Override
    public IndexingResponse getIndexing() {
        if ((siteParserService == null || siteParserService.isTerminated()) ||
                (lemmasAndIndexesParserPool == null || lemmasAndIndexesParserPool.isTerminated())) {
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
            return new IndexingResponse(true);
        } else {
            return new IndexingResponse(false, "Индексация уже запущена");
        }
    }

    private LocalDB addLemmasAndIndexes(List<Page> pagesList, SiteModel siteModel) {
        CopyOnWriteArraySet<IndexModel> indexesSet = new CopyOnWriteArraySet<>();
        ConcurrentHashMap<String, Lemma> lemmasMap = new ConcurrentHashMap<>();
        lemmasAndIndexesParserPool = new ForkJoinPool();
        CollectorParameters collectorParameters = new CollectorParameters(siteModel, pagesList, indexesSet, lemmasMap);
        return lemmasAndIndexesParserPool.invoke(new LemmaAndIndexCollector(collectorParameters));
    }

    @Override
    public IndexingResponse stopIndexing() {
        if ((siteParserService == null || siteParserService.isTerminated()) ||
                (lemmasAndIndexesParserPool == null || lemmasAndIndexesParserPool.isTerminated())) {
            return new IndexingResponse(false, "Индексация не запущена");
        } else {
            siteParserPool.shutdownNow();
            siteParserService.shutdownNow();
            if (lemmasAndIndexesParserPool != null) {
                lemmasAndIndexesParserPool.shutdownNow();
            }
            setFailedStatus(siteModel);
            return new IndexingResponse(true);
        }
    }

    @Transactional
    @Override
    public IndexingResponse indexingPage(String link) {
        String result = getResultLink(link);
        if (checkLink(result)) {
            Map<String, String> siteUrlAndName = getSiteUrlAndName(result);
            String siteUrl = siteUrlAndName.keySet().iterator().next();
            String siteName = siteUrlAndName.values().iterator().next();
            try {
                Document doc = Connection.getConnection(result);
                SiteParser siteParser = new SiteParser();
                String path = siteParser.getPath(result, siteUrl);
                int code = doc.connection().response().statusCode();
                String content = doc.html();
                siteModel = checkSite(siteName, siteUrl);
                checkPageAndRemove(path);
                repositories.getPageRepository().save(new Page(siteModel, path, code, content));
                repositories.getSiteRepository().updateStatusTime(LocalDateTime.now(), siteModel);
                postLemmasAndIndexes(repositories.getPageRepository().findPage(path, siteModel).get());
            } catch (IOException exception) {
                logger.error("Время соединения истекло");
            }
            return new IndexingResponse(true);
        } else {
            return new IndexingResponse(false,
                    "Данная страница находится за пределами сайтов," +
                            "указанных в конфигурационном файле");
        }
    }

    private String getResultLink(String link) {
        String decodeLink = java.net.URLDecoder.decode(link, StandardCharsets.UTF_8);
        String url = "url=";
        return decodeLink.substring(url.length());
    }

    private Map<String, String> getSiteUrlAndName(String result) {
        Map<String, String> siteUrlAndName = new HashMap<>();
        for (Site site : sites.getSites()) {
            String substring = result.substring(0, site.getUrl().length());
            if (substring.equals(site.getUrl())) {
                siteUrlAndName.put(site.getUrl(), site.getName());
                break;
            }
        }
        return siteUrlAndName;
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
            logger.error("Ошибка при добавлении лемм и индексов");
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
        Optional<Page> optionalPage = repositories.getPageRepository().findPage(path, siteModel);
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
