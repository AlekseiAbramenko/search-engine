package ru.skillbox;

public class Main {

    public static void main(String[] args) {

        Dimensions dimensions1 = new Dimensions(1.5, 0.8, 0.6);

        Cargo cargo1 = new Cargo(dimensions1.getSizes(),100, "Pushkina str, 25-45", true, "12435645f", true);

        System.out.println(cargo1);
        System.out.println();

//        System.out.println(cargo1.setAddress("Lenina str, 25-68"));
//        System.out.println();
//
//        System.out.println(cargo1.setWeight(50));
//        System.out.println();

//        System.out.println(dimensions1.setLength(1));
//        System.out.println(dimensions1.setWidth(2));
//        System.out.println(dimensions1.setHeight(0.5));

        System.out.println(cargo1.setWeightAndAddress(50, "Lenina str, 26-54"));
        System.out.println();

        System.out.println(dimensions1.setSizes(1,2,0.5));
    }
}