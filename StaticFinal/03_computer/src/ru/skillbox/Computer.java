package ru.skillbox;

public class Computer {
    private Processor processor;
    private Ram ram;
    private Drive drive;
    private Screen screen;
    private KeyBoard keyBoard;
    private final String vendor;
    private final String name;

    public Computer(String vendor, String name) {
        this.vendor = vendor;
        this.name = name;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public void setRam(Ram ram) {
        this.ram = ram;
    }

    public void setDrive(Drive drive) {
        this.drive = drive;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void setKeyBoard(KeyBoard keyBoard) {
        this.keyBoard = keyBoard;
    }

    public Processor getProcessor() {
        return processor;
    }

    public Ram getRam() {
        return ram;
    }

    public Drive getDrive() {
        return drive;
    }

    public Screen getScreen() {
        return screen;
    }

    public KeyBoard getKeyBoard() {
        return keyBoard;
    }

    public String getVendor() {
        return vendor;
    }

    public String getName() {
        return name;
    }

    public double getComputerWeight() {
        return processor.getWeight() + ram.getMemoryWeight()
                + drive.getDriveWeight() + screen.getScreenWeight() + keyBoard.getKeyBoardWeight();
    }

    public String toString() {
        return "Компьютер " + getVendor() + " " + getName() + "\n\n"
                + getProcessor() + "\n\n" + getRam() + "\n\n"
                + getDrive() + "\n\n" + getScreen() + "\n\n"
                + getKeyBoard() + "\n\n" + "Общий вес компьютера: "
                + getComputerWeight() + " кг.";
    }
}
