import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Loader {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        int from = 1;
        int to = 25;

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for(int i = 1; i < 5; i++) {
            executor.execute(new WorkerThread(from, to, Integer.toString(i), start));
            from += 25;
            to += 25;
        }
        executor.shutdown();
    }
}
