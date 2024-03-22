package searchengine.utils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.dto.search.SearchData;
import searchengine.model.Page;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
public class DataCollector extends RecursiveTask<List<SearchData>> {
    private Map<Page, Float> relevanceMap;
    private Map<String, String> queryLemmasMap;
    private List<SearchData> searchDataList;

    @Override
    protected List<SearchData> compute() {
        if(relevanceMap.size() > 1) {
            createSubtask().forEach(ForkJoinTask::join);
        } else {
            worker(relevanceMap);
        }
        return searchDataList;
    }

    private void worker(Map<Page, Float> relevanceMap) {
        relevanceMap.forEach((page, value) -> {
            SearchData pageData = new SearchData();
            pageData.setSite(page.getSite().getUrl());
            pageData.setSiteName(page.getSite().getName());
            pageData.setUri(page.getPath());
            pageData.setTitle(getTitle(page.getContent(), queryLemmasMap));
            pageData.setSnippet(getSnippet(page.getContent(), queryLemmasMap));
            pageData.setRelevance(value);
            searchDataList.add(pageData);
        });
    }

    private Collection<DataCollector> createSubtask() {
        List<DataCollector> taskList = new ArrayList<>();
        relevanceMap.forEach((page, value) -> {
            Map<Page, Float> onePage = new HashMap<>();
            onePage.put(page, value);
            DataCollector task = new DataCollector(onePage, queryLemmasMap, searchDataList);
            task.fork();
            taskList.add(task);
        });
        return taskList;
    }


    private String getTitle(String content, Map<String, String> queryLemmasMap) {
        Document doc = Jsoup.parse(content);
        String text = doc.title();
        Map<String, String> titleLemmasMap = getLemmasMap(text);
        Map<String, Set<String>> equalsWordsMap = getEqualsWordsMap(queryLemmasMap, titleLemmasMap);
        return getFatWordsText(text, equalsWordsMap);
    }

    @SneakyThrows
    private String getSnippet(String content, Map<String, String> queryLemmasMap) {
        String text = Jsoup.parse(content).text();
        Map<String, String> textLemmasMap = getLemmasMap(text);
        Map<String, Set<String>> equalsWordsMap = getEqualsWordsMap(queryLemmasMap, textLemmasMap);
        Map<String, Set<String>> fatEqualsWordsMap = getFatEqualsWordsMap(equalsWordsMap);
        String fatWordsText = getFatWordsText(text, equalsWordsMap);
        List<String> sentences = getSentences(fatWordsText);
        List<String> filteredSentences = sentences.stream().filter(s -> s.contains("<b>")).toList();
        int i = fatEqualsWordsMap.size();
        AtomicReference<String> snippet = new AtomicReference<>("");
        Map<String, Integer> sentencesMap = getSentencesMap(filteredSentences, fatEqualsWordsMap);
        Map.Entry<String, Integer> maxEntry =
                Collections.max(sentencesMap.entrySet(), Map.Entry.comparingByValue());
        String maxWordsSentence = maxEntry.getKey();
        if (maxEntry.getValue() == i) {
            snippet.set(maxWordsSentence);
        } else {
            Map<String, Set<String>> lastWordsMap = getLastWordsMap(fatEqualsWordsMap, maxWordsSentence);//слова, которые нужно найти и склеить
            String snippetParts = getSnippetParts(lastWordsMap, filteredSentences);
            snippet.set("..." + maxWordsSentence + snippetParts);
        }
        return String.valueOf(snippet);
    }

    @SneakyThrows
    private Map<String, String> getLemmasMap(String query) {
        LemmaParser parser = new LemmaParser();
        return parser.getLemmasMapLemmaVsQueryWord(query);
    }

    private Map<String, Set<String>> getEqualsWordsMap(Map<String, String> queryLemmasMap,
                                                       Map<String, String> textLemmasMap) {
        Map<String, Set<String>> equalsWordsMap = new HashMap<>();
        queryLemmasMap.forEach((queryNormalWord, queryWord) -> {
            equalsWordsMap.put(queryWord, new HashSet<>());
            textLemmasMap.forEach((textNormalWord, textWord) -> {
                if (textNormalWord.equals(queryNormalWord)) {
                    equalsWordsMap.forEach((k, v) -> {
                        if (k.equals(queryWord)) {
                            v.add(textWord);
                            v.add(queryWord);
                            v.add(textNormalWord);
                            String textWordToUpperCase = textWord.substring(0, 1).toUpperCase()
                                    + textWord.substring(1);
                            v.add(textWordToUpperCase);
                            String queryWordToUpperCase = queryWord.substring(0, 1).toUpperCase()
                                    + queryWord.substring(1);
                            v.add(queryWordToUpperCase);
                            String textNormalWordToUpperCase = textNormalWord.substring(0, 1).toUpperCase()
                                    + textNormalWord.substring(1);
                            v.add(textNormalWordToUpperCase);
                        }
                    });
                }
            });
        });
        return equalsWordsMap;
    }

    private String getFatWordsText(String text, Map<String, Set<String>> equalsWordsMap) {
        List<String> equalsWordsList = new ArrayList<>();
        equalsWordsMap.forEach((word, wordsList) -> {
            equalsWordsList.addAll(wordsList);
        });
        String[] replacementList = new String[equalsWordsList.size()];
        String[] searchList = new String[equalsWordsList.size()];
        for (int i = 0; i < equalsWordsList.size(); i++) {
            String word = equalsWordsList.get(i);
            searchList[i] = word;
            replacementList[i] = "<b>" + word + "</b>";
        }
        return StringUtils.replaceEach(text, searchList, replacementList);
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

    private List<String> getSentences(String sentence) {
        String[] arrWords = sentence.split(" ");
        List<String> shortSentences = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        int index = 0;
        int length = arrWords.length;
        int maxLength = 230;
        while (index != length) {
            if (count + arrWords[index].length() <= maxLength) {
                count += arrWords[index].length() + 1;
                stringBuilder.append(arrWords[index]).append(" ");
                index++;
            } else {
                shortSentences.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
                count = 0;
            }
        }
        if (!stringBuilder.isEmpty()) {
            shortSentences.add(stringBuilder.toString());
        }
        return shortSentences;
    }

    private Map<String, Integer> getSentencesMap(List<String> sentencesList,
                                                 Map<String, Set<String>> fatEqualsWordsMap) {
        Map<String, Integer> sentencesMap = new HashMap<>();
        sentencesList.forEach(sentence -> {
            AtomicInteger j = new AtomicInteger();
            fatEqualsWordsMap.forEach((word, wordsList) -> {
                for (String w : wordsList) {
                    if (sentence.contains(w)) {
                        j.addAndGet(1);
                        break;
                    }
                }
            });
            sentencesMap.put(sentence, j.intValue());
        });
        return sentencesMap;
    }

    private Map<String, Set<String>> getLastWordsMap(Map<String, Set<String>> fatEqualsWordsMap,
                                                     String maxWordsSentence) {
        Map<String, Set<String>> lastWordsMap = new HashMap<>();
        fatEqualsWordsMap.forEach((queryWord, wordsSet) -> {
            AtomicInteger count = new AtomicInteger();
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

    private String getSnippetParts(Map<String, Set<String>> lastWordsMap, List<String> sentencesList) {
        StringBuilder stringBuilder = new StringBuilder();
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
                        stringBuilder.append(sub);
                        count.addAndGet(1);
                        break;
                    }
                }
                if (count.get() > 0) {
                    break;
                }
            }
        });
        return stringBuilder.toString();
    }
}
