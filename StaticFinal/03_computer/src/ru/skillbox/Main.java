package ru.skillbox;

public class Main {

    public static void main(String[] args) {

        Computer myComputer = new Computer("Acer", "Aspire 3");
        myComputer.setProcessor(new Processor(1.66, 7, "Intel", 0.05));
        myComputer.setRam(new Ram("DDR", 8, 0.05));
        myComputer.setDrive(new Drive(DriveType.HDD, 500, 0.15));
        myComputer.setScreen(new Screen(23, ScreenType.IPS, 6));
        myComputer.setKeyBoard(new KeyBoard("мембранная", true, 0.2));

        System.out.println(myComputer);
    }
}
