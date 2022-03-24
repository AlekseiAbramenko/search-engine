package ru.skillbox;

public class Dimensions {
    private final double length;
    private final double width;
    private final double height;

    public Dimensions(double length, double width, double height) {
       this.length = length;
       this.width = width;
       this.height = height;
    }

    public Dimensions setSizes(double length, double width, double height) {
        return new Dimensions(length, width, height);
    }

    public Dimensions setLength(double length) {
        return new Dimensions(length, width, height);
    }

    public Dimensions setWidth(double width) {
        return new Dimensions(length, width, height);
    }

    public Dimensions setHeight(double height) {
        return new Dimensions(length, width, height);
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getVolume() {
        return getLength() * getWidth() * getHeight();
    }

    public String getSizes() {
        return getLength() + " * " + getWidth() + " * " + getHeight();
    }

    public String toString() {
        return getSizes();
    }
}
