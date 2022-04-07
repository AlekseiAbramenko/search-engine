package ru.skillbox;

public class Ram {
    private final String memoryType;
    private final int memoryValue;
    private final double memoryWeight;

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

    public double getMemoryWeight() {
        return memoryWeight;
    }

     public String toString() {
        return "Оперативная память" + "\n"
                + "Тип: " + getMemoryType() + "\n"
                + "Объем: " + getMemoryValue() + " ГБ" + "\n"
                + "Вес: " + getMemoryWeight() + " кг.";
    }
}