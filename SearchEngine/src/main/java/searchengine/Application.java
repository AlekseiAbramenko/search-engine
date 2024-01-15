package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

//todo: запись логов: писать в разные файлы info, error, warn
// события "операция отменена" и "...прервана" изменить на INFO

//todo: переделать поиск

//todo: написать файл readme

//todo: далее по списку от куратора