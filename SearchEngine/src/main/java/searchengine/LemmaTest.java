package searchengine;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.nio.file.Paths.get;

@RequiredArgsConstructor
public class LemmaTest {
    public static void main(String[] args) throws IOException {

//Задание 3:
//        Lemmatizator lemmatizator = new Lemmatizator();
//        Map<String, Integer> lemmas = lemmatizator.countLemmasFromText("");
//        lemmas.forEach((key, value) -> System.out.println(key + " - " + value));

////Задание 2:
//        String text = "Повторное появление леопарда в Осетии позволяет предположить," +
//                " что леопард постоянно обитает в некоторых районах Северного Кавказа. " +
//                "Леопардов много не бывает. Леопарду все ни по чем. Леопарды опасны.";
//        Lemmatizator lemmatizator = new Lemmatizator();
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
