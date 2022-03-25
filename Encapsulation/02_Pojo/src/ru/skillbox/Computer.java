package ru.skillbox;

public class Computer {
    private String name;
    private String model;
    private String processor;
    private String memory;

    public Computer(String name, String model) {
        this.name = name;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public void print() {
        System.out.println(getName());
        System.out.println(getModel());
        System.out.println(getProcessor());
        System.out.println(getMemory());
    }
}
