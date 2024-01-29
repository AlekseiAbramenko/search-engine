# Локальный поисковый сервис
___
## Описание проекта

Поисковый движок, который можно установить на своём сервере и использовать для поиска по своим сайтам или по одному из них.

___

### Функционал

* На вкладке DASHBOARD отображаются:
    - статистика
      - количество проиндексированных сайтов
      - количество проиндексированных страниц
      - количество лемм
    - названия и ссылки на сайты
    - статистика по каждому сайту

    ![dashboard](/SearchEngine/src/main/resources/pictures/dashboard.jpg)
* Вкладка MANAGEMENT используется для индексации:
    - всех сайтов из списка
    - одной страницы
  
    ![management](/SearchEngine/src/main/resources/pictures/management.jpg)
* Вкладка SEARCH используется для поиска:
    - по всем сайтам
    - по выбранному из списка

    ![search](/SearchEngine/src/main/resources/pictures/search.jpg)

### Настройки и запуск проекта
- устанавливаем MySQL community server с официального сайта: [mysql.com/downloads/](https://www.mysql.com/downloads/)
- установите пароль для root пользователя
- создайте базу данных "search_engine"
- в файле application.yaml укажите:
  - пароль для root пользователя
      ````
      spring:
          datasource:
              username: root
              password: testtest
      ````
  - нужные сайты
    ````
      indexing-settings:
        sites:
          - url: https://www.playback.ru
          name: PlayBack.Ru
          - url: https://www.svetlovka.ru
          name: Svetlovka.Ru
      ````
- запустите jar файл
- перейдите по адресу [http://localhost:8080/](http://localhost:8080/)

### Инструменты, библиотеки и фреймворки
- Git — система управления версиями;
- Apache Maven — для автоматизации сборки проектов;
- Spring Framework — универсальный фреймворк как основа для наших Java-приложений;
- Spring Boot - проект, который упрощает создание приложений на основе Spring;
- Spring Web — модуль, включающий необходимые компоненты для веб-приложения и имеющий 
  встроенный контейнер сервлетов Apache Tomcat;
- Spring Data JPA — библиотека, используемая для взаимодействия с сущностями базы данных;
- MySQL — реляционная система управления базами данных;
- Hibernate — фреймворк для работы с базами данных;
- Lombok — проект по добавлению дополнительной функциональности в Java c помощью 
  изменения исходного кода перед компиляцией;
