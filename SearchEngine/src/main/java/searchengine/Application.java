package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

//todo: разобраться, почему парсятся не все страницы когда индексирую группу сайтов
//запускать индексацию сайтов последовательно
//останавливать потоки через boolean переменную, чтобы не было исключений

//todo: избавиться от дублирования кода! добавление лемм в сайтпарсере и индексингсервисе
//todo: написать файл readme
//todo: далее по списку от куратора