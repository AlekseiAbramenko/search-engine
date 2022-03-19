public class Arithmetic {
    int a, b;

    public Arithmetic(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int calculateSum() {
        int sum = a + b;
        return sum;
    }

    public int calculateProizvedenie() {
        int proizvedenie = a * b;
        return proizvedenie;
    }

    public int bolsheeChislo() {
        int max;
        if (a > b) {
            max = a;
        } else {
            max = b;
        }
        return max;
    }

    public int mensheeChislo() {
        int min;
        if (a < b) {
            min = a;
        } else {
            min = b;
        }
        return min;
    }

    public void print() {
        System.out.println("Задание 2");
        System.out.println("Сумма чисел: " + calculateSum());
        System.out.println("Произведение чисел: " + calculateProizvedenie());
        System.out.println("Большее число: " + bolsheeChislo());
        System.out.println("Меньшее число: " + mensheeChislo());
    }
}