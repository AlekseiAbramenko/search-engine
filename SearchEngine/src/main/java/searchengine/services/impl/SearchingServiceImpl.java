package searchengine.services.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteModel;
import searchengine.config.Repositories;
import searchengine.workers.DataListMaker;
import searchengine.workers.LemmasParser;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchingServiceImpl implements searchengine.services.SearchingService {
    @Autowired
    private Repositories repositories;
    private final Logger logger = LoggerFactory.getLogger(SearchingServiceImpl.class);

    public SearchResponse getSearching(RequestParameters requestParam) {
        SearchResponse response = new SearchResponse();
        String siteUrl = requestParam.getSite();
        Map<String, String> queryLemmasMap = getLemmasMap(requestParam.getQuery());
        Map<Lemma, Integer> lemmasFrequency = getLemmasFrequency(queryLemmasMap, siteUrl);
        if (lemmasFrequency.isEmpty()) {
            response.setData(null);
            response.setCount(0);
        } else {
            Map<Lemma, Integer> sortedLemmasMap = getSortedLemmasMap(lemmasFrequency);
            List<Page> pagesList = getPagesList(lemmasFrequency, siteUrl, sortedLemmasMap);
            if (pagesList.isEmpty()) {
                response.setData(null);
                response.setCount(0);
                logger.error("пустой список страниц");
            } else {
                Map<Page, Float> relevanceMap = getRelevance(pagesList, sortedLemmasMap, requestParam.getLimit());
                List<SearchData> searchDataList = getSearchDataList(relevanceMap, queryLemmasMap);
                int count = searchDataList.size();
                SearchData[] allData = new SearchData[count];
                for (int i = 0; i < count; i++) {
                    allData[i] = searchDataList.get(i);
                }
                response.setCount(count);
                response.setData(allData);
            }
        }
        return response;
    }

    private List<Page> getPagesList(Map<Lemma, Integer> lemmasFrequency, String siteUrl,
                                    Map<Lemma, Integer> sortedLemmasMap) {
        List<Page> pagesList;
        if (lemmasFrequency.size() == 1) {
            pagesList = getPagesListForSingleWord(sortedLemmasMap, siteUrl);
        } else if (siteUrl == null) {
            pagesList = getPagesListForAllSites(sortedLemmasMap);
        } else {
            pagesList = getPagesListForSingleSite(sortedLemmasMap, siteUrl);
        }
        return pagesList;
    }

    private Map<Lemma, Integer> getLemmasFrequency(Map<String, String> queryLemmasMap, String siteUrl) {
        Map<Lemma, Integer> lemmasFrequency = new HashMap<>();
        queryLemmasMap.forEach((normalWord, word) -> {
            List<Lemma> lemmaList = null;
            if (siteUrl == null) {
                lemmaList = repositories.getLemmaRepository().findLemmasListByName(normalWord);
            } else {
                Optional<SiteModel> siteModelOptional = repositories.getSiteRepository().findSiteByUrl(siteUrl);
                if (siteModelOptional.isPresent()) {
                    SiteModel siteModel = siteModelOptional.get();
                    lemmaList = repositories.getLemmaRepository().findLemmasList(normalWord, siteModel);
                }
            }
            assert lemmaList != null;
            lemmaList.forEach(lemma -> {
                if (lemma.getFrequency() < 100) {
                    lemmasFrequency.put(lemma, lemma.getFrequency());
                }
            });
        });
        return lemmasFrequency;
    }

    @SneakyThrows
    private List<SearchData> getSearchDataList(Map<Page, Float> relevanceMap, Map<String, String> queryLemmasMap) {
        List<SearchData> searchDataList = new ArrayList<>();
        List<Callable<SearchData>> dataListMakers = new ArrayList<>();
        relevanceMap.forEach((key, value) -> {
            dataListMakers.add(new DataListMaker(key, value, queryLemmasMap));
        });
        ExecutorService service = Executors.newFixedThreadPool(8);
        List<Future<SearchData>> futures = service.invokeAll(dataListMakers);
        futures.forEach(future -> {
            try {
                searchDataList.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("При создании DataList возникла ошибка или поток был прерван.");
                throw new RuntimeException(e);
            }
        });
        return searchDataList;
    }

    private Map<Page, Float> getRelevance(List<Page> pagesFinalList, Map<Lemma, Integer> sortedLemmasMap, int limit) {
        Map<Page, Float> absRelevanceMap = new HashMap<>();
        Map<Page, Float> relevanceMap = new HashMap<>();
        if (pagesFinalList.size() > 1) {
            pagesFinalList.forEach(page -> {
                sortedLemmasMap.keySet().forEach(lemma -> {
                    Optional<IndexModel> optionalIndexModel = repositories.getIndexRepository().findIndex(page, lemma);
                    if (optionalIndexModel.isPresent()) {
                        float rank = optionalIndexModel.get().getRank();
                        absRelevanceMap.merge(page, rank, Float::sum);
                    }
                });
            });
            float maxValue = Collections.max(absRelevanceMap.entrySet(), Map.Entry.comparingByValue()).getValue();
            absRelevanceMap.forEach((key, value) -> {
                float newValue = value / maxValue;
                relevanceMap.put(key, newValue);
            });
        } else {
            relevanceMap.put(pagesFinalList.getFirst(), 1F);
        }
        return getSortedPagesMap(relevanceMap, limit);
    }

    private Map<Page, Float> getSortedPagesMap(Map<Page, Float> unsortedMap, int limit) {
        if (!unsortedMap.isEmpty()) {
            return unsortedMap.entrySet().stream()
                    .sorted(Map.Entry.<Page, Float>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        } else {
            return null;
        }
    }

    private List<Page> getPagesListForSingleWord(Map<Lemma, Integer> sortedLemmasMap, String siteUrl) {
        List<IndexModel> indexes;
        Lemma lemma = sortedLemmasMap.keySet().iterator().next();
        if (siteUrl == null) {
            indexes = repositories.getIndexRepository().findIndexesByLemma(lemma);
        } else {
            indexes = repositories.getIndexRepository().findPagesByLemmaBySite(lemma, siteUrl);
        }
        List<Page> pages = new ArrayList<>();
        indexes.forEach(index -> pages.add(index.getPage()));
        return pages;
    }

    private List<Page> getPagesListForSingleSite(Map<Lemma, Integer> sortedLemmasMap, String siteUrl) {
        Lemma firstLemma = sortedLemmasMap.keySet().iterator().next();
        List<IndexModel> indexes = repositories.getIndexRepository().
                findPagesByLemmaBySite(firstLemma, siteUrl);
        List<Page> pages = new ArrayList<>();
        indexes.forEach(index -> pages.add(index.getPage()));
        return getPagesFinalListSingleSite(pages, firstLemma, sortedLemmasMap);
    }

    private List<Page> getPagesListForAllSites(Map<Lemma, Integer> sortedLemmasMap) {
        List<Page> pages = new ArrayList<>();
        Map<String, Map<List<Lemma>, Integer>> unionLemmasMap = getUnionLemmasMap(sortedLemmasMap);
        Map<String, Integer> sortedUnionLemmasMap = getSortedUnionLemmasMap(unionLemmasMap);
        String minUnionFrequencyLemma = sortedUnionLemmasMap.entrySet().iterator().next().getKey();
        List<IndexModel> indexes = getIndexes(unionLemmasMap, minUnionFrequencyLemma);
        indexes.forEach(index -> pages.add(index.getPage()));
        return getPagesFinalList(pages, minUnionFrequencyLemma, unionLemmasMap);
    }

    private List<Page> getPagesFinalList(List<Page> pages, String minUnionFrequencyLemma,
                                         Map<String, Map<List<Lemma>, Integer>> unionLemmasMap) {
        List<Page> pagesFinalList = new ArrayList<>(pages);
        Map<String, Map<List<Lemma>, Integer>> unionLemmasMapWithoutFirstLemma =
                new LinkedHashMap<>(unionLemmasMap);
        unionLemmasMapWithoutFirstLemma.remove(minUnionFrequencyLemma);
        unionLemmasMapWithoutFirstLemma.forEach((name, map) -> {
            pages.forEach(page -> {
                if (pagesFinalList.contains(page)
                        && !repositories.getIndexRepository().existsIndex2(page, name)) {
                    pagesFinalList.remove(page);
                }
            });
        });
        return pagesFinalList;
    }

    private List<Page> getPagesFinalListSingleSite(List<Page> pages, Lemma firstLemma,
                                                   Map<Lemma, Integer> sortedLemmasMap) {
        List<Page> pagesFinalList = new ArrayList<>(pages);
        Map<Lemma, Integer> sortedMapWithoutFirstLemma = new LinkedHashMap<>(sortedLemmasMap);
        sortedMapWithoutFirstLemma.remove(firstLemma);
        sortedMapWithoutFirstLemma.keySet().forEach(lemma -> {
            pages.forEach(page -> {
                if (pagesFinalList.contains(page)
                        && !repositories.getIndexRepository().existsIndex(page, lemma)) {
                    pagesFinalList.remove(page);
                }
            });
        });
        return pagesFinalList;
    }

    private List<IndexModel> getIndexes(Map<String, Map<List<Lemma>, Integer>> unionLemmasMap,
                                        String minUnionFrequencyLemma) {
        List<IndexModel> indexes = new ArrayList<>();
        unionLemmasMap.get(minUnionFrequencyLemma).forEach((list, freq) -> {
            list.forEach(l -> {
                List<IndexModel> temporaryList =
                        repositories.getIndexRepository().findIndexesByLemma(l);
                indexes.addAll(temporaryList);
            });
        });
        return indexes;
    }

    private Map<String, Integer> getSortedUnionLemmasMap(Map<String, Map<List<Lemma>, Integer>> unionLemmasMap) {
        Map<String, Integer> unsortedMap = new LinkedHashMap<>();
        unionLemmasMap.forEach((name, map) -> {
            map.forEach((list, freq) -> {
                unsortedMap.put(name, freq);
            });
        });
        return unsortedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private Map<String, Map<List<Lemma>, Integer>> getUnionLemmasMap(Map<Lemma, Integer> sortedLemmasMap) {
        Map<String, Map<List<Lemma>, Integer>> unionLemmasMap = new HashMap<>();
        sortedLemmasMap.forEach((lemma, frequency) -> {
            if (!unionLemmasMap.containsKey(lemma.getLemma())) {
                Map<List<Lemma>, Integer> lemmasListAndFrequency = new HashMap<>();
                List<Lemma> lemmasList = new ArrayList<>();
                lemmasList.add(lemma);
                lemmasListAndFrequency.put(lemmasList, lemma.getFrequency());
                unionLemmasMap.put(lemma.getLemma(), lemmasListAndFrequency);
            } else {
                unionLemmasMap.get(lemma.getLemma()).forEach((localLemmasList, localFrequency) -> {
                    Map<List<Lemma>, Integer> newLemmasListAndFrequency = new HashMap<>();
                    List<Lemma> newLemmasList = new ArrayList<>(localLemmasList);
                    newLemmasList.add(lemma);
                    Integer newFrequency = localFrequency + lemma.getFrequency();
                    newLemmasListAndFrequency.put(newLemmasList, newFrequency);
                    unionLemmasMap.put(lemma.getLemma(), newLemmasListAndFrequency);
                });
            }
        });

        return unionLemmasMap;
    }

    private Map<Lemma, Integer> getSortedLemmasMap(Map<Lemma, Integer> unsortedMap) {
        return unsortedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    @SneakyThrows
    private Map<String, String> getLemmasMap(String query) {
        LemmasParser parser = new LemmasParser();
        return parser.getLemmasMapLemmaVsQueryWord(query);
    }
}