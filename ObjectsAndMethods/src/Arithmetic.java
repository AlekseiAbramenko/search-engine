public class Arithmetic {
    int a, b, max, min;
    int sum = 0;
    int proizvedenie = 0;

    public Arithmetic(int a, int b) {
        System.out.println("Задание 2:");
        this.a = a;
        this.b = b;
    }

    public void calculateSum() {
        sum = a + b;
        System.out.println("Сумма чисел: " + sum);
    }

    public void calculateProizvedenie() {
        proizvedenie = a * b;
        System.out.println("Произведение чисел: " + proizvedenie);
    }

    public void bolsheeChislo() {
        if (a > b) {
            max = a;
        } else {
            max = b;
        }
        System.out.println("Большее число: " + max);
    }

    public void mensheeChislo() {
        if (a < b) {
            min = a;
        } else {
            min = b;
        }
        System.out.println("Меньшее число: " + min);
    }
}