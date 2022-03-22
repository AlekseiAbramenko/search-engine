package ru.skillbox;

final class Book {
    private final String name;
    private final String writer;
    private final int pagesCount;
    private final String isbnNumber;

    public Book(String name, String writer, int pagesCount, String isbnNumber) {
        this.name = name;
        this.writer = writer;
        this.pagesCount = pagesCount;
        this.isbnNumber = isbnNumber;
    }

    public String getName() {
        return name;
    }

    public String getWriter() {
        return writer;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public String getIsbnNumber() {
        return isbnNumber;
    }
}
