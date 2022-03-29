package ru.skillbox;

public class Main {

    public static void main(String[] args) {

        ArithmeticCalculator calculator = new ArithmeticCalculator(2, 3);
        System.out.println(calculator.calculate(Operation.ADD));
        System.out.println(calculator.calculate(Operation.SUBTRACT));
        System.out.println(calculator.calculate(Operation.MULTIPLY));
    }
}
