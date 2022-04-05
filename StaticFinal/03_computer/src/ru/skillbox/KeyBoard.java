package ru.skillbox;

public class KeyBoard {
    private final String keyBoardType;
    private final boolean keyBoardIllumination;
    private final double keyBoardWeight;

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

    public double getKeyBoardWeight() {
        return keyBoardWeight;
    }

    public String toString() {
        return "Клавиатура" + "\n"
                + "Тип: " + getKeyBoardType() + "\n"
                + "Подсветка: " + isKeyBoardIllumination() + "\n"
                + "Вес: " + getKeyBoardWeight() + " кг.";
    }
}

