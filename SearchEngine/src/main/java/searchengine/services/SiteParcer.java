package searchengine.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;

import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class SiteParcer extends RecursiveAction {
    private List<SiteParcer> taskList;
    private final SiteModel siteModel;
    private final String link;
    private final CopyOnWriteArraySet<String> linksList;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    @Override
    public void compute() {
        if(getPool().isShutdown()) {
            taskList.clear();
        } else {
            try {
                taskList = new ArrayList<>();
                Document doc = getConnection(link);
                Thread.sleep(500);

                String path = link.replace(siteModel.getUrl(), "/");
                Integer cod = doc.connection().response().statusCode();
                String content = doc.html();
                if(!pageRepository.existPage(path)) {
                    postPage(cod, content, path, siteModel);
                }
                Elements elements = doc.select("a[abs:href^=" + link + "]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link)) {
                        linksList.add(link);
                        SiteParcer task = new SiteParcer(
                                siteModel, link, linksList, pageRepository, siteRepository);
                        task.fork();
                        taskList.add(task);
                    }
                });
                taskList.forEach(ForkJoinTask::join);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkLink(String url) {
        return !linksList.contains(url)
                && !url.contains("#")
                && !url.contains(".pdf")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".php");
    }

    @Transactional
    public void postPage(Integer cod, String content, String path, SiteModel siteModel) {
        Page page = new Page();
        page.setSite(siteModel);
        page.setPath(path);
        page.setCode(cod);
        page.setContent(content);
        pageRepository.save(page);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    public Document getConnection(String link) {
        try {
            return Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(10000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}