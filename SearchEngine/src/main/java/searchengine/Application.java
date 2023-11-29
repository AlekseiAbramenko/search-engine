package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

//todo: класс с репозиториями или класс с параметрами для парсера

//todo: в случае ошибок выдавать понятные ексепшны
// и возвращать корректные коды ошибок

//todo: выложить прогу и базу в сеть, сделать доступ по ссылке

//todo: написать файл readme и отправить на проверку куратору