package ru.skillbox;

public class Drive {
    private final DriveType driveType;
    private final int driveValue;
    private final double driveWeight;
    public static double getDriveWeight;

    public Drive(DriveType driveType, int driveValue, double driveWeight) {
        this.driveType = driveType;
        this.driveValue = driveValue;
        this.driveWeight = driveWeight;
        getDriveWeight = driveWeight;
    }

    public DriveType getDriveType() {
        return driveType;
    }

    public int getDriveValue() {
        return driveValue;
    }

    public double getDriveWeight() {
        return driveWeight;
    }

    public String getDrive() {
        return "Жесткий диск" + "\n"
                + "Тип: " + getDriveType() + "\n"
                + "Объем: " + getDriveValue() + " ГБ" + "\n"
                + "Вес: " + getDriveWeight() + " кг.";
    }

    public String toString() {
        return getDrive();
    }
}
