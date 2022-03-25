package ru.skillbox;

public class Cargo {
    private final Dimensions dimensions;
    private final double weight;
    private final String address;
    private final boolean flip;
    private final String number;
    private final boolean fragile;

    public Cargo(Dimensions dimensions, double weight, String address, boolean flip, String number, boolean fragile) {
        this.dimensions = dimensions;
        this.weight = weight;
        this.address = address;
        this.flip = flip;
        this.number = number;
        this.fragile = fragile;
    }

    public Cargo setWeightAndAddress(double weight, String address) {
        return new Cargo(dimensions, weight, address, flip, number, fragile);
    }

    public Cargo setWeight(double weight) {
       return new Cargo(dimensions, weight, address, flip, number, fragile);
    }

    public Cargo setAddress(String address) {
        return new Cargo(dimensions, weight, address, flip, number, fragile);
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public double getWeight() {
        return weight;
    }

    public String getAddress() {
        return address;
    }

    public boolean getFlip() {
        return flip;
    }

    public String getNumber() {
        return number;
    }

    public boolean getFragile() {
        return fragile;
    }

    public String toString() {
        return getDimensions() + "\n" + getWeight() + "\n" + getAddress() + "\n"
                + getFlip() + "\n" + getNumber() + "\n" + getFragile();
    }
}
