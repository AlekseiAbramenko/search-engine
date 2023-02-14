package src.main;

import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Date;
import java.util.Random;

public class RedisStorage {
    private RedissonClient redisson;
    private RKeys rKeys;
    private static final String KEY = "USERS";
    private RScoredSortedSet<String> users;
    private static final int SLEEP = 1000;
    private static final int USERS_COUNT = 20;
    private static final Random random = new Random();
    private double getTime() {
        return new Date().getTime();
    }

    void init() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException ex) {
            System.out.println("Не удаётся подключиться к Redis");
            System.out.println(ex.getMessage());
        }
        users = redisson.getScoredSortedSet(KEY);
    }

    void userRegistration(int user_id) {
        users.add(getTime(), String.valueOf(user_id));
    }

    void setUsers() {
        for(int i = 1; i <= USERS_COUNT; i++) {
            userRegistration(i);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void showingUsers() throws InterruptedException {
        int count = 0;
        for(String user : users) {
            System.out.println("-На главной странице показываем пользователя " + user);
            count++;
            Thread.sleep(SLEEP);
            if(count == 10) {
                paidUsesShowing();
                count = 0;
            }
        }
    }
    void paidUsesShowing() throws InterruptedException {
        int userNumber = random.nextInt(1, USERS_COUNT + 1);
        System.out.println("> Пользователь " + userNumber + " оплатил платную услугу");
        Thread.sleep(SLEEP);
        System.out.println("— На главной странице показываем пользователя " + userNumber);
        Thread.sleep(SLEEP);
    }

    void shutdown() {
        redisson.shutdown();
    }
}
