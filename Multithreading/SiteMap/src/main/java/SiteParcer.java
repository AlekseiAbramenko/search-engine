import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SiteParcer extends RecursiveTask<CopyOnWriteArraySet<String>> {

    private String link;
    private CopyOnWriteArraySet<String> linksList;

    public SiteParcer(String link, CopyOnWriteArraySet<String> linksList) {
        this.link = link;
        this.linksList = linksList;
    }

    @Override
    protected CopyOnWriteArraySet<String> compute() {
        List<SiteParcer> taskList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("Chrome/81.0.4044.138")
                    .timeout(100000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();
            Thread.sleep(150);
            Elements elements = doc.select("a[abs:href^=" + link + "]");
            elements.forEach(element -> {
                String link = element.absUrl("href");
                if (checkLink(link)) {
                    linksList.add(link);
                    SiteParcer task = new SiteParcer(link, linksList);
                    task.fork();
                    taskList.add(task);
                }
            });
            taskList.forEach(ForkJoinTask::join);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linksList;
    }

    public boolean checkLink(String url) {
        return !linksList.contains(url)
                && !url.contains("#")
                && !url.contains(".pdf")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".php");
    }
}
