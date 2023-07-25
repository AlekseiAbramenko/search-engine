package searchengine.services;

import lombok.AllArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class LemmasParcer {
    private final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    public HashMap<String, Integer> countLemmasFromText(String html) throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        HashMap<String, Integer> lemmas = new HashMap<>();

        String text = Jsoup.parse(html).text();
        String[] words = arrayContainsRussianWords(text);

        for(String word : words) {
            if(word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = morphology.getMorphInfo(word);
            if(anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = morphology.getNormalForms(word);
            if(normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);
            if(normalWord.length() < 2) {
                continue;
            }

            if(lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) +1);
            } else {
                lemmas.put(normalWord, 1);
            }
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
