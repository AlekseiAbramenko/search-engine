package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//todo: индексация одной страницы, решить вопрос! (проверить)
//todo: ДУБЛИ!!! Решение: при поиске возвращать list.getFirst
// либо, добавлять по одной, проверяя наличие в базе!

//todo: skillbox индексировать
//todo: скорость поиска проверить

//todo: в siteParser.checkLink сделать массив!

//todo: сниппет в 3 строки, уже есть решение (более длинные предложения)
//todo: поставить проверку на общую индексацию в контроллере на pageIndexing

//todo: сократить метод siteParser.compute
//todo: переделать dataListMaker через fjp
//todo: не добавлять страницы без слэша на конце!?