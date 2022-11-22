package core;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Bank {

    private Map<String, Account> accounts = new HashMap<>();
    private final Random random = new Random();
    private List<String> stopList = new ArrayList<>();

    public Bank() {
        setAccounts();
    }

    public void setAccounts() {
        for (int i = 0; i < 100; i++) {
            String accNumber = String.valueOf(random.nextLong(0, 2000000000));
            long money = random.nextLong(0, 100000);
            accounts.put(accNumber, new Account(money, accNumber));
        }
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public String getAccountNumber() {
        Set<String> keySet = accounts.keySet();
        List<String> accNumbers = new ArrayList<>(keySet);

        return accNumbers.get(random.nextInt(0, accNumbers.size()));
    }

    public int getAmount() {
        return random.nextInt(1, 52650);
    }

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    /**
     * TODO: реализовать метод. Метод переводит деньги между счетами. Если сумма транзакции > 50000,
     * то после совершения транзакции, она отправляется на проверку Службе Безопасности – вызывается
     * метод isFraud. Если возвращается true, то делается блокировка счетов (как – на ваше
     * усмотрение)
     */
    public void transfer(String fromAccountNum, String toAccountNum, long amount) {
        if (stopList.contains(fromAccountNum) || stopList.contains(toAccountNum)) {
            return;
        }

        synchronized (accounts) {
            if (accounts.get(fromAccountNum).getMoney() >= amount) {
                accounts.get(fromAccountNum).setMoney(getBalance(fromAccountNum) - amount);
                accounts.get(toAccountNum).setMoney(getBalance(toAccountNum) + amount);
            } else {
                return;
            }

            if (amount > 50000) {
                try {
                    if (isFraud(fromAccountNum, toAccountNum, amount)) {
                        stopList.add(fromAccountNum);
                        stopList.add(toAccountNum);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * TODO: реализовать метод. Возвращает остаток на счёте.
     */
    public long getBalance(String accountNum) {
        return accounts.get(accountNum).getMoney();
    }

    public long getSumAllAccounts() {
        AtomicLong sum = new AtomicLong();

        accounts.forEach((s, account) -> sum.addAndGet(account.getMoney()));

        return sum.get();
    }
}
