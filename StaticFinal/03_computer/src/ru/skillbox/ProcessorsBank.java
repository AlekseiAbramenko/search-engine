package ru.skillbox;

public class ProcessorsBank {

    public static Processor IntellCoreI5() {
        return new Processor(1.66, 5, "Intel", 0.021);
    }
    public static Processor IntellCoreI7() {
        return new Processor(2.44, 7, "Intel", 0.021);
    }
}