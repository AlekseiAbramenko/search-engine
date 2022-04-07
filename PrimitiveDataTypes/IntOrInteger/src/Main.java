public class Main {
    public static void main(String[] args) {
        Container container = new Container();
        container.addCount(5672);
        System.out.println(container.getCount());

        // TODO: ниже напишите код для выполнения задания:
        //  С помощью цикла и преобразования чисел в символы найдите все коды
        //  букв русского алфавита — заглавных и строчных, в том числе буквы Ё.

        for (char i = 'А'; i != 'я'; i++) {
            int c = i;
            System.out.println(c + " - " + i);
        }

        char c = 'я';
        int a = c;
        System.out.println(a + " - " + c);

        c = 'ё';
        a = c;
        System.out.println(a + " - " + c);

        c = 'Ё';
        a = c;
        System.out.println(a + " - " + c);
    }
}