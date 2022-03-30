package ru.skillbox;

public class Screen {
    private final int screenDiagonal;
    private final ScreenType screenType;
    private final double screenWeight;
    public static double getScreenWeight;

    public Screen(int screenDiagonal, ScreenType screenType, double screenWeight) {
        this.screenDiagonal = screenDiagonal;
        this.screenType = screenType;
        this.screenWeight = screenWeight;
        getScreenWeight = screenWeight;
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

    public String getScreen() {
        return "Экран" + "\n"
                + "Диагональ: " + getScreenDiagonal() + " ''" + "\n"
                + "Тип: " + getScreenType() + "\n"
                + "Вес: " + getScreenWeight() + " кг.";
    }

    public String toString() {
        return getScreen();
    }
}
