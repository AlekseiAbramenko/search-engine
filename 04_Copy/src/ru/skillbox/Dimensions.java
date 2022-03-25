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

    public Dimensions setDimensions(double length, double width, double height) {
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

    public String getDimensions() {
        return getLength() + " * " + getWidth() + " * " + getHeight();
    }

    public String toString() {
        return getDimensions();
    }
}
