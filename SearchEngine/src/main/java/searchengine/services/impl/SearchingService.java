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

    public SearchResponseTrue getSearching(String query) {
        SearchResponseTrue response = new SearchResponseTrue();
        Map<String, String> queryLemmasMap = lemmasParser(query);

//        List<String> lemmas = lemmasParser(query);
        Map<Lemma, Integer> lemmasFrequency = new HashMap<>();

        queryLemmasMap.forEach((normalWord, word) -> {
            if(lemmaRepository.findLemmaByName(normalWord).isPresent()) {
                Lemma lemma = lemmaRepository.findLemmaByName(normalWord).get();
                lemmasFrequency.put(lemma, lemma.getFrequency());
            }
        });

        Map<Lemma, Integer> sortedLemmasMap = getSortedLemmasMap(lemmasFrequency);
        List<Page> pagesFinalList = getPagesList(sortedLemmasMap);
        Map<Page, Float> relevanceMap = getRelevance(pagesFinalList, sortedLemmasMap);
        List<SearchData> searchDataList = getSearchDataList(relevanceMap, queryLemmasMap);
        int count = searchDataList.size();
        SearchData[] allData = new SearchData[count];

        for (int i = 0; i < searchDataList.size(); i++) {
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
            pageData.setSnippet(getSnippet(key.getContent(), queryLemmasMap)); //todo: сделать
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
        Map<String, String> textLemmasMap = lemmasParser(text); //текст(нормальное слово, слово в тексте)
        Map<String, Set<String>> equalsWordsMap = new HashMap<>(); //запрос(слово в запросе, его словоформы)

        //делаем карту: слово из запроса; слово из запроса + его словоформы в тексте + все с большой буквы
        queryLemmasMap.forEach((queryNormalWord, queryWord) -> {
            equalsWordsMap.put(queryWord, new HashSet<>());
            textLemmasMap.forEach((textNormalWord, textWord) -> {
                if (textNormalWord.equals(queryNormalWord)) {
                    equalsWordsMap.forEach((k, v) -> {
                        if(k.equals(queryWord)) {
                            v.add(textWord);
                            v.add(queryWord);
                            v.add(textNormalWord);
                            //todo: запихнуть в отдельный метод "с большой буквы"
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

//отсюда в новый метод "сделать жирным"

        //из карты в список слов из запроса с возможными словоформами в тексте + все слова с большой буквы
        List<String> equalsWordsList = new ArrayList<>();
        equalsWordsMap.forEach((word, wordsList) -> {
            equalsWordsList.addAll(wordsList);
        });
//        equalsWordsList.forEach(System.out::println); //проверяем, что получилось в списке

        String[] replacementList = new String[equalsWordsList.size()];
        String[] searchList = new String[equalsWordsList.size()];

        for(int i=0; i<equalsWordsList.size(); i++) {
            String word = equalsWordsList.get(i);
            searchList[i] = word;
            replacementList[i] = "<b>" + word + "</b>";
        }

        String text1 = StringUtils.replaceEach(text, searchList, replacementList);

//до сюда метод "сделать жирным"

        String[] sentences = text1.split("[.,?!]");//текст разбитый на предложения
        List<String> sentencesList = Arrays.stream(sentences).filter(s -> s.contains("<b>")).toList();//только предложения, где слова выделены жирным
        sentencesList.forEach(System.out::println); //проверка

        //делаем новую карту, где все словоформы жирным шрифтом
        Map<String, Set<String>> fatEqualsWordsMap = new HashMap<>();

        equalsWordsMap.forEach((word, wordsList) -> {
          Set<String> newSet = new HashSet<>();
          wordsList.forEach(w -> {
              String newWord = "<b>" + w + "</b>";
              newSet.add(newWord);
          });
          fatEqualsWordsMap.put(word, newSet);
        });

//        fatEqualsWordsMap.forEach((k, v) -> {
//            System.out.println(k + ": " + v);
//        });


        //считаем количество вхождений искомых слов в предложениях
        int i = fatEqualsWordsMap.size(); //количество слов в запросе
        System.out.println("i= " + i);
        AtomicReference<String> snippet = new AtomicReference<>("");
        Map<String, Integer> sentencesMap = new HashMap<>();//предложение - количество вхождений

        sentencesList.forEach(sentence -> {
            String[] sentencesWords = sentence.split(" "); //разбил предложения на слова
            //todo: зачем разбивать предложение на слова, если можно использовать contains?
            AtomicInteger j = new AtomicInteger();//счетчик найденных слов в предложении

            fatEqualsWordsMap.forEach((word, wordsList) -> {
                wordsList.forEach(w -> {
                    for(String sentencesWord : sentencesWords) {
                        if (w.equals(sentencesWord)) {
                            j.addAndGet(1);
                            break;
                        }
                    }
                });
            });

            System.out.println("j= " + j);
            if(j.get()==i) {
                snippet.set(sentence); //todo: здесь прерывать метод и возвращать сниппет!
            } else {
                sentencesMap.put(sentence, j.intValue());//предложение - количество совпадений
            }
        });

        Map.Entry<String, Integer> maxEntry =
                Collections.max(sentencesMap.entrySet(), Map.Entry.comparingByValue());

        String maxWordsSentence = maxEntry.getKey();
        snippet.set("..." + maxWordsSentence);

//        System.out.println("Предложение с максимальным числом вхождений: " + maxEntry);//проверка

        Map<String, Set<String>> lastWords = new HashMap<>();//слова, которые нужно найти и склеить

        fatEqualsWordsMap.forEach((queryWord, wordsSet) -> {//этот код встречается уже второй раз (строка 169), закинуть в отдельный метод
            AtomicInteger count = new AtomicInteger();
//            System.out.println("начальное значение count: " + count);//проверка
            wordsSet.forEach(word -> {
                if(maxWordsSentence.contains(word)) {
                  count.addAndGet(1);
                }
            });
            if(count.get() == 0) {
                lastWords.put(queryWord, wordsSet);
            }
        });
//        System.out.println("осталось найти слов: " + lastWords.size());
//
//        lastWords.forEach((k,v) -> {
//            System.out.println(k + " " + v);
//        });
        //ищем оставшиеся слова
        lastWords.forEach((word, wordsSet) -> {//этот код встречается уже третий раз (строки 169 и 198), закинуть в отдельный метод
            wordsSet.forEach(w -> {
                sentencesList.forEach(sentence -> {
                    if(sentence.contains(w)) {
                        int start = sentence.indexOf(w) - 20;
                        int end = start + w.length() + 20;
                        String sub = "..." + sentence.substring(start, end) + "...";
                        snippet.set(snippet + sub);
                    }
                });
            });
        });

        //todo: размер сниппета <= 3 строки

        return String.valueOf(snippet);
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
    private Map<String, String> lemmasParser(String query) {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        Map<String, String> lemmasMap = new HashMap<>();
//        List<String> lemmas = new ArrayList<>();
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
//            lemmas.add(normalWord);
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
