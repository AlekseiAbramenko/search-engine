package ru.skillbox;

public class Processor {
    private final double processorFrequency;
    private final int coresCount;
    private final String manufacturer;
    private static double weight;

    public Processor(double processorFrequency, int coresCount, String manufacturer, double weight) {
        this.processorFrequency = processorFrequency;
        this.coresCount = coresCount;
        this.manufacturer = manufacturer;
        this.weight = weight;
    }

    public double getProcessorFrequency() {
        return processorFrequency;
    }

    public int getCoresCount() {
        return coresCount;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public static double getWeight() {
        return weight;
    }

    public String getProcessor() {
        return "Процессор" + "\n"
                + "Частота: " + getProcessorFrequency() + " МГц" + "\n"
                + "Количество ядер: " + getCoresCount() + "\n"
                + "Производитель: " + getManufacturer() + "\n"
                + "Вес: " + getWeight() + " кг.";
    }

    public String toString() {
        return getProcessor();
    }
}
