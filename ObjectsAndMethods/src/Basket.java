public class Basket {
    private String items = "";
    private int totalPrice = 0;
    private int limit;
    private double totalWeight = 0;

    public void add(String name, int price) {
        add(name, price, 1);
    }

    public void add(String name, int price, int count) {
        add(name, price, count, 0.0);
    }

    public void add(String name, int price, int count, double weight) {
        price *= count;
        weight *= count;

        if (conteins(name)) {
            return;
        }
        if (totalPrice + price >= limit) {
            return;
        }

        items = items + "\n" + name + " - " + count + " шт. - " + price + " - " + weight + " кг.";
        totalPrice += price;
        totalWeight += weight;
    }

    public Basket() {
        items = "Список товаров:";
        this.limit = 1000000;
    }

    public Basket(int limit) {
        this();
        this.limit = limit;
    }

    public Basket(String items, int totalPrice) {
        this();
        this.items = this.items + items;
        this.totalPrice = totalPrice;
    }

    public void clear() {
        items = "";
        totalPrice = 0;
        totalWeight = 0;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean conteins(String name) {
        return items.contains(name);
    }

    public void print(String title) {
        System.out.println(title);
        if (items.isEmpty()) {
            System.out.println("Корзина пуста");
        } else {
            System.out.println(items);
            System.out.println("");
            System.out.println("Общая стоимость товаров: " + getTotalPrice() + " руб." +
                    "\n" + "Общий вес товаров: " + getTotalWeight() + " кг.");
            System.out.println("");
        }
    }
}
