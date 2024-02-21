package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//todo: skillbox индексировать
// страницы писать сразу в базу

//todo: сниппет в 3 строки, уже есть решение (более длинные предложения)!!!
//todo: поставить проверку на общую индексацию в контроллере на pageIndexing

//todo: сократить метод siteParser.compute
//todo: переделать dataListMaker через fjp
//todo: не добавлять страницы без слэша на конце!?
//todo: сделать метод, который будет проверять наличие дублей после индексации и удалять их!?