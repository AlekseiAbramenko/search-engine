package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//todo: добавить ответы tru и false для поиска
//todo: добавить параметры: site, offset, limit для поиска
//todo: учитывать по всем сайтам поиск или по выбранному
//todo: настроить фронт для поиска
//todo: в случае ошибок выдавать понятные ексепшены
//todo: написать тесты для разных частей проложения