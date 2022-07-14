import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerStorage {
    private final Map<String, Customer> storage;

    public CustomerStorage() {
        storage = new HashMap<>();
    }

    public void addCustomer(String data) throws IllegalArgumentException {
        final int INDEX_NAME = 0;
        final int INDEX_SURNAME = 1;
        final int INDEX_EMAIL = 2;
        final int INDEX_PHONE = 3;

        final String phoneRegex = "[+]7[0-9]{10}";
        final Pattern emailRegex = Pattern.compile("[A-z]+\\.?[A-z]*@[A-z]+\\.[ru]?[com]?");

        String[] components = data.split("\\s+");
        Matcher matcher = emailRegex.matcher(components[INDEX_EMAIL]);

        if (components.length != 4) {
            throw new IllegalArgumentException("Wrong adding customer format. Correct format: " +
                    "add Василий Петров vasily.petrov@gmail.com +79215637722");
        } else if (!matcher.find()) {
            throw new IllegalArgumentException("Wrong adding email format. Correct format: vasily.petrov@gmail.com");
        } else if (!components[INDEX_PHONE].matches(phoneRegex)) {
            throw new IllegalArgumentException("Wrong adding phone format. Correct format: +79215637722");
        } else {
            String name = components[INDEX_NAME] + " " + components[INDEX_SURNAME];
            storage.put(name, new Customer(name, components[INDEX_PHONE], components[INDEX_EMAIL]));
            System.out.println("Customer successfully added.");
        }
    }

    public void listCustomers() {
        storage.values().forEach(System.out::println);
    }

    public void removeCustomer(String name) {
        storage.remove(name);
    }

    public Customer getCustomer(String name) {
        return storage.get(name);
    }

    public int getCount() {
        return storage.size();
    }
}