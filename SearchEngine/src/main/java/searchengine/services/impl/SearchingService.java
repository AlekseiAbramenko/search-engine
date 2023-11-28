package searchengine.services.impl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponseTrue;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchingService implements searchengine.services.SearchingService {
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "ПРЕДК", "МС"};

    public SearchResponseTrue getSearching(RequestParameters requestParam) {
        SearchResponseTrue response = new SearchResponseTrue();
        Map<String, String> queryLemmasMap = lemmasParser(requestParam.getQuery());
        Map<Lemma, Integer> lemmasFrequency = new HashMap<>();

        queryLemmasMap.forEach((normalWord, word) -> {
            if(lemmaRepository.findLemmaByName(normalWord).isPresent()) {
                Lemma lemma = lemmaRepository.findLemmaByName(normalWord).get();
                lemmasFrequency.put(lemma, lemma.getFrequency());
            }
        });

        Map<Lemma, Integer> sortedLemmasMap = getSortedLemmasMap(lemmasFrequency);
        List<Page> pagesFinalList = getPagesList(sortedLemmasMap, requestParam.getSite());
        Map<Page, Float> relevanceMap = getRelevance(pagesFinalList, sortedLemmasMap);
        List<SearchData> searchDataList = getSearchDataList(relevanceMap, queryLemmasMap);
        int count = Math.min(requestParam.getLimit(), searchDataList.size());
        SearchData[] allData = new SearchData[count];

        for (int i = 0; i < count; i++) {
            allData[i] = searchDataList.get(i);
        }
        response.setCount(count);
        response.setData(allData);
        return response;
    }

    private List<SearchData> getSearchDataList(Map<Page, Float> relevanceMap, Map<String, String> queryLemmasMap) {
        List<SearchData> searchDataList = new ArrayList<>();
        relevanceMap.forEach((key, value) -> {
            SearchData pageData = new SearchData();
            pageData.setSite(key.getSite().getUrl());
            pageData.setSiteName(key.getSite().getName());
            pageData.setUri(key.getPath());
            pageData.setTitle(getTitle(key.getContent()));
            pageData.setSnippet(getSnippet(key.getContent(), queryLemmasMap));
            pageData.setRelevance(value);
            searchDataList.add(pageData);
        });
        return searchDataList;
    }

    private String getTitle(String content) {
        Document doc = Jsoup.parse(content);
        return doc.title();
    }

    @SneakyThrows
    private String getSnippet(String content, Map<String, String> queryLemmasMap) {
        String text = Jsoup.parse(content).text();
        Map<String, String> textLemmasMap = lemmasParser(text);//нормальное слово, слово в тексте
        Map<String, Set<String>> equalsWordsMap = getEqualsWordsMap(queryLemmasMap, textLemmasMap); //слово из запроса; слово из запроса + его словоформы в тексте + все с большой буквы
        Map<String, Set<String>> fatEqualsWordsMap = getFatEqualsWordsMap(equalsWordsMap);//все словоформы жирным шрифтом
        String fatWordsText = getFatWordsText(text, equalsWordsMap); //слова из запроса в тексте жирным шрифтом
        String[] sentences = fatWordsText.split("[.,?!]");//текст разбитый на предложения
        List<String> shortSentences = getShortSentencesList(sentences);//длинные предложения разбитые на короткие
        List<String> sentencesList = shortSentences.stream().filter(s -> s.contains("<b>")).toList();//только предложения, где слова выделены жирным

//считаем количество вхождений искомых слов в предложениях
        int i = fatEqualsWordsMap.size(); //количество слов в запросе
        AtomicReference<String> snippet = new AtomicReference<>("");
        Map<String, Integer> sentencesMap = new HashMap<>();//предложение - количество вхождений
        sentencesList.forEach(sentence -> {
            AtomicInteger j = new AtomicInteger();//счетчик найденных слов в предложении
            fatEqualsWordsMap.forEach((word, wordsList) -> {
                for (String w : wordsList) {
                    if (sentence.contains(w)) {
                        j.addAndGet(1);
                        break;
                    }
                }
            });
            if (j.get() == i) {
                snippet.set(sentence);
            } else {
                sentencesMap.put(sentence, j.intValue());//предложение - количество совпадений
            }
        });

        if (snippet.get().length() > 0) {
            return String.valueOf(snippet);
        } else {
            Map.Entry<String, Integer> maxEntry =
                    Collections.max(sentencesMap.entrySet(), Map.Entry.comparingByValue());
            String maxWordsSentence = maxEntry.getKey();
            snippet.set("..." + maxWordsSentence);
            Map<String, Set<String>> lastWordsMap = getLastWordsMap(fatEqualsWordsMap, maxWordsSentence);//слова, которые нужно найти и склеить

//ищем оставшиеся слова
            //todo: отсюда в отдельный метод: возвращием строку и склеиваем со сниппетом
            lastWordsMap.forEach((word, wordsSet) -> {
                for (String w : wordsSet) {
                    AtomicInteger count = new AtomicInteger();
                    for (String sentence : sentencesList) {
                        if (sentence.contains(w)) {
                            int start = sentence.indexOf(w);
                            if (start >= 20) {
                                start -= 20;
                            } else {
                                start = 0;
                            }
                            int end = start + w.length();
                            end += Math.min((sentence.length() - end), 20);
                            String sub = "..." + sentence.substring(start, end) + "...";
                            snippet.set(snippet + sub);
                            count.addAndGet(1);
                            break;
                        }
                    }
                    if (count.get() > 0) {
                        break;
                    }
                }
            });
            return String.valueOf(snippet);
        }
    }

    private List<String> getShortSentencesList(String[] sentences) {
        List<String> shortSentences = new ArrayList<>();

        for (String sentence : sentences) {
            if(sentence.length() > 150) {
                List<String> sentList = getShortSentences(sentence);
                shortSentences.addAll(sentList);
            } else {
                shortSentences.add(sentence);
            }
        }
        return shortSentences;
    }

    private List<String> getShortSentences(String sentence) {
        String[] arrWords = sentence.split(" ");
        List<String> shortSentences = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        int index = 0;
        int length = arrWords.length;
        int maxLength = 150;

        while (index != length) {
            if(count + arrWords[index].length() <= maxLength) {
                count += arrWords[index].length() + 1;
                stringBuilder.append(arrWords[index]).append(" ");
                index++;
            } else {
                shortSentences.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
                count = 0;
            }
        }
        if(stringBuilder.length() > 0) {
            shortSentences.add(stringBuilder.toString());
        }
        return shortSentences;
    }

    private Map<String, Set<String>> getLastWordsMap(Map<String, Set<String>> fatEqualsWordsMap, String maxWordsSentence) {
        Map<String, Set<String>> lastWordsMap = new HashMap<>();//слова, которые нужно найти и склеить
        fatEqualsWordsMap.forEach((queryWord, wordsSet) -> {
            AtomicInteger count = new AtomicInteger();
//            System.out.println("начальное значение count: " + count);//проверка
            wordsSet.forEach(word -> {
                if (maxWordsSentence.contains(word)) {
                    count.addAndGet(1);
                }
            });
            if (count.get() == 0) {
                lastWordsMap.put(queryWord, wordsSet);
            }
        });

        return lastWordsMap;
    }

    private Map<String, Set<String>> getFatEqualsWordsMap(Map<String, Set<String>> equalsWordsMap) {
        Map<String, Set<String>> fatEqualsWordsMap = new HashMap<>();
        equalsWordsMap.forEach((word, wordsList) -> {
            Set<String> newSet = new HashSet<>();
            wordsList.forEach(w -> {
                String newWord = "<b>" + w + "</b>";
                newSet.add(newWord);
            });
            fatEqualsWordsMap.put(word, newSet);
        });

        return fatEqualsWordsMap;
    }

    private String getFatWordsText(String text, Map<String, Set<String>> equalsWordsMap) {
        List<String> equalsWordsList = new ArrayList<>();
        equalsWordsMap.forEach((word, wordsList) -> {
            equalsWordsList.addAll(wordsList);
        });

        String[] replacementList = new String[equalsWordsList.size()];
        String[] searchList = new String[equalsWordsList.size()];

        for(int i=0; i<equalsWordsList.size(); i++) {
            String word = equalsWordsList.get(i);
            searchList[i] = word;
            replacementList[i] = "<b>" + word + "</b>";
        }
        return StringUtils.replaceEach(text, searchList, replacementList);
    }

    private Map<String, Set<String>> getEqualsWordsMap(Map<String, String> queryLemmasMap,
                                                       Map<String, String> textLemmasMap) {
        Map<String, Set<String>> equalsWordsMap = new HashMap<>();
        queryLemmasMap.forEach((queryNormalWord, queryWord) -> {
            equalsWordsMap.put(queryWord, new HashSet<>());
            textLemmasMap.forEach((textNormalWord, textWord) -> {
                if (textNormalWord.equals(queryNormalWord)) {
                    equalsWordsMap.forEach((k, v) -> {
                        if(k.equals(queryWord)) {
                            v.add(textWord);
                            v.add(queryWord);
                            v.add(textNormalWord);
                            String textWordToUpperCase = textWord.substring(0,1).toUpperCase()
                                    + textWord.substring(1);
                            v.add(textWordToUpperCase);
                            String queryWordToUpperCase = queryWord.substring(0,1).toUpperCase()
                                    + queryWord.substring(1);
                            v.add(queryWordToUpperCase);
                            String textNormalWordToUpperCase = textNormalWord.substring(0,1).toUpperCase()
                                    + textNormalWord.substring(1);
                            v.add(textNormalWordToUpperCase);
                        }
                    });
                }
            });
        });

        return equalsWordsMap;
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
                float newValue = value / maxValue;
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

    private List<Page> getPagesList(Map<Lemma, Integer> sortedLemmasMap, String siteUrl) {
        Lemma firstLemma = sortedLemmasMap.entrySet().iterator().next().getKey();
        List<IndexModel> indexes;

        if(!(siteUrl==null)) {
            indexes = indexRepository.findPagesByLemmaBySite(firstLemma, siteUrl);
        } else {
            indexes = indexRepository.findPagesByLemma(firstLemma);
        }

        List<Page> pages = new ArrayList<>();
        indexes.forEach(index -> pages.add(index.getPage()));
        List<Page> pagesFinalList = new ArrayList<>(pages);
        Map<Lemma, Integer> sortedMapWithoutFirstLemma = new LinkedHashMap<>(sortedLemmasMap);
        sortedMapWithoutFirstLemma.remove(firstLemma);
        sortedMapWithoutFirstLemma.keySet().forEach(lemma -> {
            pages.forEach(page -> {
                if (pagesFinalList.contains(page) && !indexRepository.existsIndex(page, lemma)) {
                    pagesFinalList.remove(page);
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
    private Map<String, String> lemmasParser(String query) {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        Map<String, String> lemmasMap = new HashMap<>();
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
            lemmasMap.put(normalWord, word);
        }
        return lemmasMap;
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
