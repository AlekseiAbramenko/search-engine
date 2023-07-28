package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//todo: настроить зависимости (при удалении сайта, удалять его страницы, леммы, индексы)

//todo: написать тесты для разных частей проложения