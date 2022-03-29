package ru.skillbox;

public class ArithmeticCalculator {
    public int a;
    public int b;
    public int result;

    public ArithmeticCalculator(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int calculate(Operation operation) {
        if(operation == Operation.ADD) {
            result = a + b;
        }

        if(operation == Operation.SUBTRACT) {
            result = a - b;
        }

        if(operation == Operation.MULTIPLY) {
            result = a * b;
        }
        return result;
    }
}
