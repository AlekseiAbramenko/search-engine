package ru.skillbox;

public class KeyBoard {
    private final String keyBoardType;
    private final boolean keyBoardIllumination;
    private final double keyBoardWeight;
    public static double getKeyboardWeight;

    public KeyBoard(String keyBoardType, boolean keyBoardIllumination, double keyBoardWeight) {
        this.keyBoardType = keyBoardType;
        this.keyBoardIllumination = keyBoardIllumination;
        this.keyBoardWeight = keyBoardWeight;
        getKeyboardWeight = keyBoardWeight;
    }

    public String getKeyBoardType() {
        return keyBoardType;
    }

    public boolean isKeyBoardIllumination() {
        return keyBoardIllumination;
    }

    public double getKeyBoardWeight() {
        return keyBoardWeight;
    }

    public String getKeyBoard() {
        return "Клавиатура" + "\n"
                + "Тип: " + getKeyBoardType() + "\n"
                + "Подсветка: " + isKeyBoardIllumination() + "\n" // TODO: воткнуть тернарный оператор
                + "Вес: " + getKeyBoardWeight() + " кг.";
    }

    public String toString() {
        return getKeyBoard();
    }
}

