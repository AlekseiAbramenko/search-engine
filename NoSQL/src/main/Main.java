package src.main;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        RedisStorage redis = new RedisStorage();
        redis.init();
        redis.setUsers();

        while (true) {
            redis.showingUsers();
        }
    }
}