package ru.skillbox;

public class KeyBoard {
    private final String keyBoardType;
    private final boolean keyBoardIllumination;
    private static double keyBoardWeight;

    public KeyBoard(String keyBoardType, boolean keyBoardIllumination, double keyBoardWeight) {
        this.keyBoardType = keyBoardType;
        this.keyBoardIllumination = keyBoardIllumination;
        this.keyBoardWeight = keyBoardWeight;
    }

    public String getKeyBoardType() {
        return keyBoardType;
    }

    public String isKeyBoardIllumination() {
        return keyBoardIllumination ? "да" : "нет";
    }

    public static double getKeyBoardWeight() {
        return keyBoardWeight;
    }

    public String getKeyBoard() {
        return "Клавиатура" + "\n"
                + "Тип: " + getKeyBoardType() + "\n"
                + "Подсветка: " + isKeyBoardIllumination() + "\n"
                + "Вес: " + getKeyBoardWeight() + " кг.";
    }

    public String toString() {
        return getKeyBoard();
    }
}

