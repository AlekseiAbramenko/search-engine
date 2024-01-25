package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

//поиск стал долше работать!!! посмотреть по поиску, где можно уменьшить кол-во запросов к базе

//todo: удалить ненужные logger.error в siteParser и searchingServiceImpl

//todo: написать файл readme

//todo: jar файл сделать

//todo: видео с экрана по всему функционалу: особенно поиск по 2м, 3м, 1му слову, потом переиндексация

//todo: работа доступна для публичного доступа по url

//todo: почему не работает удаление индексов по сайту?