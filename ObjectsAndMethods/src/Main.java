public class Main {
    public static void main(String[] args) {
        Basket mashaBasket = new Basket();
        mashaBasket.add("Iphone 13 pro", 250000, 1, 0.15);
        mashaBasket.add("Адаптер", 5000, 1, 0.05);
        mashaBasket.add("Чехол", 3000, 1, 0.02);
        mashaBasket.add("Защитное стекло", 2000, 1, 0.01);

        mashaBasket.print("Корзина Маши:");

        Arithmetic alexArithmetic = new Arithmetic(11, 12);
        alexArithmetic.print();

        Printer alexPrinter = new Printer();
        alexPrinter.append("Текст документа", "Имя документа", 15);
        alexPrinter.append("Текст документа 1", "Имя документа 1", 16);
        alexPrinter.print();
    }
}