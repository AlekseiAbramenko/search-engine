package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LemmaTest {
    public static void main(String[] args) throws IOException {
//        LuceneMorphology morphology = new RussianLuceneMorphology();
//        List<String> wordBaseForms = morphology.getMorphInfo("ваш");
//        System.out.println(wordBaseForms);

//Задание 3:
//        Lemmatizator lemmatizator = new Lemmatizator();
//        Map<String, Integer> lemmas = lemmatizator.countLemmasFromText("");
//        lemmas.forEach((key, value) -> System.out.println(key + " - " + value));

//Задание 2:
//        String text = "Разве я герой? Вот герой. Именно я горой. Я почти герой.";
//        LemmasParcer lemmatizator = new LemmasParcer();
//        Map<String, Integer> lemmas = lemmatizator.countLemmasFromText(text);
//        lemmas.forEach((key, value) -> System.out.println(key + " - " + value));
//// Задание 1:
//        getNormalForms("леса");
    }

    public static void getNormalForms(String word) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> wordBaseForms = luceneMorph.getNormalForms(word);
        wordBaseForms.forEach(System.out::println);
    }
}
