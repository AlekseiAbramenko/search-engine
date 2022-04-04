package ru.skillbox;

public class Ram {
    private final String memoryType;
    private final int memoryValue;
    private static double memoryWeight;

    public Ram(String memoryType, int memoryValue, double memoryWeight) {
        this.memoryType = memoryType;
        this.memoryValue = memoryValue;
        this.memoryWeight = memoryWeight;
    }

    public String getMemoryType() {
        return memoryType;
    }

    public int getMemoryValue() {
        return memoryValue;
    }

    public static double getMemoryWeight() {
        return memoryWeight;
    }

    public String getRam() {
        return "Оперативная память" + "\n"
                + "Тип: " + getMemoryType() + "\n"
                + "Объем: " + getMemoryValue() + " ГБ" + "\n"
                + "Вес: " + getMemoryWeight() + " кг.";
    }

    public String toString() {
        return getRam();
    }
}
