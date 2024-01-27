package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//todo: при переиндексации страницы frequency должен сначала уменьшаться, а потом восстанавливаться, а сейчас он просто увеличился
//todo: почему при первой индексации постоянно не индексируется одна и та же лемма???

//todo: прогнать еще раз по всем функциям, убедиться, что нет ошибок!

//todo: видео с экрана по всему функционалу: особенно поиск по 2м, 3м, 1му слову, потом переиндексация

//todo: написать файл readme

//todo: jar файл сделать

//todo: работа доступна для публичного доступа по url

//todo: почему не работает удаление индексов по сайту?