package ru.skillbox;

public class Main {

    public static void main(String[] args) {

        Computer myComputer = new Computer("Acer", "Aspire 3");
        myComputer.setProcessor(ProcessorsStorage.IntelCoreI7);
        myComputer.setRam(RamStorage.KingstonDDRR4);
        myComputer.setDrive(DriveStorage.SeagateBarracuda_ST6000DM003);
        myComputer.setScreen(ScreenStorage.Samsung_S24R356FHI);
        myComputer.setKeyBoard(KeyBoardStorage.Logitech_K400);

        System.out.println(myComputer);
    }
}
