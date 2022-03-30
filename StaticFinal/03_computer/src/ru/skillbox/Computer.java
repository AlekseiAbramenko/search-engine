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

    public Processor setProcessor(ProcessorsStorage processor) {
        if (processor == ProcessorsStorage.IntelCoreI5) {
            this.processor = new Processor(1.66, 5, "Intel", 0.021);
        }
        if (processor == ProcessorsStorage.IntelCoreI7) {
            this.processor = new Processor(2.44, 7, "Intel", 0.021);
        }
        return this.processor;
    } //TODO: перенести объекты в отдельные классы

    public Ram setRam(RamStorage ram) {
        if (ram == RamStorage.PatriotSignatureDDR3) {
            this.ram = new Ram("DDR3", 4, 0.015);
        }
        if (ram == RamStorage.KingstonDDRR4) {
            this.ram = new Ram("DDR4", 8, 0.015);
        }
        return this.ram;
    }

    public Drive setDrive(DriveStorage drive) {
        if (drive == DriveStorage.SeagateBarracuda_ST500LM030) {
            this.drive = new Drive(DriveType.HDD, 500, 0.09);
        }
        if (drive == DriveStorage.SeagateBarracuda_ST6000DM003) {
            this.drive = new Drive(DriveType.HDD, 6000, 0.61);
        }
        return this.drive;
    }

    public Screen setScreen(ScreenStorage screen) {
        if (screen == ScreenStorage.ASUS_TUF_Gaming_VG27VQ) {
            this.screen = new Screen(24, ScreenType.IPS, 3.4);
        }
        if (screen == ScreenStorage.Samsung_S24R356FHI) {
            this.screen = new Screen(27, ScreenType.VA, 6);
        }
        return this.screen;
    }

    public KeyBoard setKeyBoard(KeyBoardStorage keyBoard) {
        if (keyBoard == KeyBoardStorage.Logitech_K380) {
            this.keyBoard = new KeyBoard("мембранная", false, 0.423);
        }
        if (keyBoard == KeyBoardStorage.Logitech_K400) {
            this.keyBoard = new KeyBoard("мембранная", true, 0.425);
        }
        return this.keyBoard;
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
        return Processor.getProcessorWeight + Ram.getRamWeight
                + Drive.getDriveWeight + Screen.getScreenWeight + KeyBoard.getKeyboardWeight;
    }

    public String getComputer() {
        return "Компьютер " + getVendor() + " " + getName() + "\n\n"
                + getProcessor() + "\n\n" + getRam() + "\n\n"
                + getDrive() + "\n\n" + getScreen() + "\n\n"
                + getKeyBoard() + "\n\n" + "Общий вес компьютера: "
                + getComputerWeight() + " кг.";
    }

    public String toString() {
        return getComputer();
    }
}
