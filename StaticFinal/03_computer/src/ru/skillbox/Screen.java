package ru.skillbox;

public class Screen {
    private final int screenDiagonal;
    private final ScreenType screenType;
    private final double screenWeight;

    public Screen(int screenDiagonal, ScreenType screenType, double screenWeight) {
        this.screenDiagonal = screenDiagonal;
        this.screenType = screenType;
        this.screenWeight = screenWeight;
    }

    public int getScreenDiagonal() {
        return screenDiagonal;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public double getScreenWeight() {
        return screenWeight;
    }

    public String toString() {
        return "Экран" + "\n"
                + "Диагональ: " + getScreenDiagonal() + " ''" + "\n"
                + "Тип: " + getScreenType() + "\n"
                + "Вес: " + getScreenWeight() + " кг.";
    }
}
