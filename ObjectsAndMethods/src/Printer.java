public class Printer {
    String queue = "";
    int pendingPagesCount = 0;
    int totalPrintingPages = 0;

    public void append(String text) {
        append(text, " ");
    }

    public void append(String text, String name) {
        append(text, name, 1);
    }

    public void append(String text, String name, int count) {
        pendingPagesCount = count;
        queue = queue + "\n" + name + "\n" + text + "\n" + count + " стр." + "\n";
        totalPrintingPages += pendingPagesCount;
    }

    public Printer() {
        queue = "\n" + "Задание 3.";
    }

    public void clear() {
        queue = "";
    }

    public void print() {
        System.out.println(queue);
        clear();
        System.out.println("Добавлено: " + getPendingPagesCount() + " стр." +
                "\n" + "Всего распечатано: " + getTotalPrintingPages() + " стр.");
    }

    public int getPendingPagesCount() {
        return pendingPagesCount;
    }

    public int getTotalPrintingPages() {
        return totalPrintingPages;
    }
}
