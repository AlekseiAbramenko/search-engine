package searchengine.services.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponseTrue;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchingService implements searchengine.services.SearchingService {
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "ПРЕДК", "МС"};
    public SearchResponseTrue getSearching(String query) {
        SearchResponseTrue response = new SearchResponseTrue();
        List<String> lemmas = lemmasParser(query);
        Map<Lemma, Integer> lemmasFrequency = new HashMap<>();

        lemmas.forEach(word -> {
            if(lemmaRepository.findLemmaByName(word).isPresent()) {
                Lemma lemma = lemmaRepository.findLemmaByName(word).get();
                lemmasFrequency.put(lemma, lemma.getFrequency());
            }
        });

        Map<Lemma, Integer> sortedLemmasMap = getSortedLemmasMap(lemmasFrequency);
        List<Page> pagesFinalList = getPagesList(sortedLemmasMap);

        System.out.println("list size after: " + pagesFinalList.size());//test

        Map<Page, Float> relevanceMap = getRelevance(pagesFinalList, sortedLemmasMap);

        relevanceMap.forEach((key, value) -> {
            System.out.println("page " + key.getId() + " absRelevance: " + value);
        });//test

        return response;
    }

    private Map<Page, Float> getRelevance(List<Page> pagesFinalList, Map<Lemma, Integer> sortedLemmasMap) {
        Map<Page, Float> absRelevanceMap = new HashMap<>();
        Map<Page, Float> relevanceMap = new HashMap<>();
        AtomicInteger absRelevance = new AtomicInteger();

        if (pagesFinalList.size() > 1) {
            pagesFinalList.forEach(page -> {
                sortedLemmasMap.keySet().forEach(lemma -> {
                    if(indexRepository.findIndex(page, lemma).isPresent()) {
                        int rank = (int) indexRepository.findIndex(page, lemma).get().getRank();
                        absRelevance.addAndGet(rank);
                    }
                });
                absRelevanceMap.put(page, absRelevance.floatValue());
                absRelevance.set(0);
            });
            float maxValue = Collections.max(absRelevanceMap.entrySet(), Map.Entry.comparingByValue()).getValue();
            absRelevanceMap.forEach((key, value) -> {
                float newValue =  Math.round((value / maxValue) * 100F) / 100F;
                relevanceMap.put(key, newValue);
            });
        } else if (pagesFinalList.size() == 1) {
            relevanceMap.put(pagesFinalList.get(0), 1F);
        } else {
            relevanceMap.put(null, null);
        }
        return getSortedPagesMap(relevanceMap);
    }

    private Map<Page, Float> getSortedPagesMap(Map<Page, Float> unsortedMap){
        if(!unsortedMap.isEmpty()) {
            return unsortedMap.entrySet().stream()
                    .sorted(Map.Entry.<Page, Float> comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        } else {
            return null;
        }
    }

    private List<Page> getPagesList(Map<Lemma, Integer> sortedLemmasMap) {
        sortedLemmasMap.forEach((key, value) -> System.out.println(key.getLemma() + " " + value));//test
        Lemma firstLemma = sortedLemmasMap.entrySet().iterator().next().getKey();
        System.out.println("first lemma: " + firstLemma.getLemma()); //test
        List<IndexModel> indexes = indexRepository.findPagesByLemma(firstLemma);
//        indexes.forEach(indexModel -> System.out.println(indexModel.getId()));
        List<Page> pages = new ArrayList<>();
        indexes.forEach(index -> pages.add(index.getPage()));
//        pages.forEach(page -> System.out.println(page.getId()));
        System.out.println("pages size: " + pages.size());
        List<Page> pagesFinalList = new ArrayList<>(pages);
        Map<Lemma, Integer> sortedMapWithoutFirstLemma = new LinkedHashMap<>(sortedLemmasMap);
        sortedMapWithoutFirstLemma.remove(firstLemma);
        System.out.println("list size :" + pagesFinalList.size());//test
        sortedMapWithoutFirstLemma.keySet().forEach(lemma -> {
            pages.forEach(page -> {
                if (pagesFinalList.contains(page) && !indexRepository.existsIndex(page, lemma)) {
                    pagesFinalList.remove(page);
                    System.out.println("Промежуточный размер листа: " + pagesFinalList.size());
                }
            });
        });
        return pagesFinalList;
    }

    private Map<Lemma, Integer> getSortedLemmasMap(Map<Lemma, Integer> unsortedMap){
        return unsortedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    @SneakyThrows
    private List<String> lemmasParser(String query) {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        List<String> lemmas = new ArrayList<>();
        String[] words = arrayContainsRussianWords(query);

        for(String word : words) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = morphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = morphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);
            if (normalWord.length() < 2) {
                continue;
            }

            lemmas.add(normalWord);
        }
        return lemmas;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^А-яёЁ\\s]+", "")
                .trim()
                .split("\\s");
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for(String property : particlesNames) {
            if(wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
}
