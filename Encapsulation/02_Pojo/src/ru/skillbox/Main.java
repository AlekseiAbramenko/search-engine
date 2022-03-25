package ru.skillbox;

public class Main {

    public static void main(String[] args) {
        Country nativeCountry = new Country("Russia");
        nativeCountry.setName("Russia");
        nativeCountry.setCapital("Moscow");
        nativeCountry.setArea(17125191);
        nativeCountry.setPopulationSize(145478097);
        nativeCountry.setSeaAccess(true);

        nativeCountry.print();

        Computer myComputer = new Computer("macbook", "pro");
        myComputer.setName("macbook");
        myComputer.setModel("pro");
        myComputer.setProcessor("4,0 ГГц, 12 core");
        myComputer.setMemory("1 ТБ");

        myComputer.print();
    }
}