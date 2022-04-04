package ru.skillbox;

public class Screen {
    private final int screenDiagonal;
    private final ScreenType screenType;
    private static double screenWeight;

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

    public static double getScreenWeight() {
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
