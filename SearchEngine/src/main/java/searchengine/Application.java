package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

//todo: грузить все пачками
//todo: индексация одной страницы, решить вопрос!
//todo: сниппет в 3 строки, уже есть решение (более длинные предложения)