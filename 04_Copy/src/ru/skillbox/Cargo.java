package ru.skillbox;

public class Cargo {
    private final String sizes;
    private final double weight;
    private final String address;
    private final boolean flip;
    private final String number;
    private final boolean fragile;

    public Cargo(String sizes, double weight, String address, boolean flip, String number, boolean fragile) {
        this.sizes = sizes;
        this.weight = weight;
        this.address = address;
        this.flip = flip;
        this.number = number;
        this.fragile = fragile;
    }

    public Cargo setWeight(double weight) {
       return new Cargo(sizes, weight, address, flip, number, fragile);
    }

    public Cargo setAddress(String address) {
        return new Cargo(sizes, weight, address, flip, number, fragile);
    }

    public String getSizes() {
        return sizes;
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
        return getSizes() + "\n" + getWeight() + "\n" + getAddress() + "\n"
                + getFlip() + "\n" + getNumber() + "\n" + getFragile();
    }
}
