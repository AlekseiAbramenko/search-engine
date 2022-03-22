package ru.skillbox;

public class Main {

    public static void main(String[] args) {

        Book myBook = new Book("Java 8 для начинающих", "Герберт Шилдт",
                810, "978-5-8459-1955-7");
        System.out.println(myBook.getName());
        System.out.println(myBook.getWriter());
        System.out.println(myBook.getPagesCount());
        System.out.println(myBook.getIsbnNumber());
        System.out.println();

        Product myProduct = new Product("Java 8 для начинающих", "9_785_961_448_016");
        myProduct.setPrice(2750);
        System.out.println(myProduct.getName());
        System.out.println(myProduct.getBarCode());
        System.out.println(myProduct.getPrice());
    }
}
