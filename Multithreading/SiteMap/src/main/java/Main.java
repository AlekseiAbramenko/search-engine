import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        String link = "https://skillbox.ru/";
        CopyOnWriteArraySet<String> linksList = new CopyOnWriteArraySet<>();

        List<String> sortedList = new ArrayList<>(new ForkJoinPool().invoke(new SiteParcer(link, linksList)));
        Collections.sort(sortedList);
        writeFile(sortedList);
    }

    public static void writeFile(List<String> sortedList) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("data/links.txt", true));
            sortedList.forEach(link -> {
                int count = StringUtils.countMatches(link, "/");
                String tabs = String.join("", Collections.nCopies(count - 3, "\t"));
                writer.write(tabs + link + "\n");
                writer.flush();
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
