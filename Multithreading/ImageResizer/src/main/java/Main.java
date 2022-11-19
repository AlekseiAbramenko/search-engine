import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    private static final int targetWidth = 500;
    private static final int cores = Runtime.getRuntime().availableProcessors();
    private static final long start = System.currentTimeMillis();

    public static void main(String[] args) {
        String srcFolder = "C:\\Users\\Aleksei\\Skillbox\\Multithreading\\ImageResizer\\src\\main\\resources\\src";
        String dstFolder = "C:\\Users\\Aleksei\\Skillbox\\Multithreading\\ImageResizer\\src\\main\\resources\\dst";

        File srcDir = new File(srcFolder);

        List<File> files = Arrays.asList(Objects.requireNonNull(srcDir.listFiles()));

        int partitionSize = files.size() / cores;

        List<List<File>> partitions = new ArrayList<>();
        for (int i = 0; i < files.size(); i += partitionSize) {
            partitions.add(files.subList(i, Math.min(i + partitionSize, files.size())));
        }

        for (List<File> partition : partitions) {
            new imageResizer(partition, targetWidth, dstFolder, start).start();
        }
    }
}
