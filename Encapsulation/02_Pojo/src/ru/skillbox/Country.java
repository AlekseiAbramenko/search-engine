package ru.skillbox;

public class Country {
    private String name;
    private int populationSize;
    private int area;
    private String capital;
    private boolean seaAccess;

    public Country(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public boolean getSeaAccess() {
        return seaAccess;
    }

    public void setSeaAccess(boolean seaAccess) {
        this.seaAccess = seaAccess;
    }

    public void print() {
        System.out.println(getName());
        System.out.println(getCapital());
        System.out.println(getArea());
        System.out.println(getPopulationSize());
        System.out.println(getSeaAccess());
    }
}
