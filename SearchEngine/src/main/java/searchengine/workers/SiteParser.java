package searchengine.workers;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.Connection;
import searchengine.config.Repositories;
import searchengine.model.PageParameters;
import searchengine.model.Page;

import searchengine.model.SiteModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SiteParser extends RecursiveTask<List<Page>> {
    private Repositories repositories;
    private List<SiteParser> taskList;
    private PageParameters parsingParameters;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;
    private final String[] removal = new String[] {"#", "?utm", ".pdf", ".PDF", ".png", ".jpg", ".jpeg", ".JPG", ".xlsx", ".doc", ".eps", ".php"};
    private final Logger logger = LoggerFactory.getLogger(SiteParser.class);

    public SiteParser(PageParameters parsingParameters, Repositories repositories) {
        this.parsingParameters = parsingParameters;
        this.repositories = repositories;
    }

    public SiteParser() {
    }

    @Override
    public List<Page> compute() {
        if (getPool().isShutdown() && taskList != null) {
            taskList.clear();
        } else {
            try {
                String url = parsingParameters.getUrl();
                SiteModel siteModel = parsingParameters.getSiteModel();
                linksSet = parsingParameters.getLinksSet();
                pagesSet = parsingParameters.getPagesSet();
                taskList = new ArrayList<>();
                Document doc = Connection.getConnection(url);
                Thread.sleep(500);
                int code = doc.connection().response().statusCode();
                String content = doc.html();
                String path = getPath(url, siteModel.getUrl());
                if(code == 200 && !url.equals(siteModel.getUrl())) {
                    pagesSet.add(new Page(siteModel, path, code, content));
//        repositories.getSiteRepository().updateStatusTime(LocalDateTime.now(), siteModel);
                }
                Elements elements = doc.select("a[href]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link, siteModel.getUrl())) {
                        linksSet.add(link);
                        PageParameters newParsingParameters = new PageParameters(siteModel, link, linksSet, pagesSet);
                        SiteParser task = new SiteParser(newParsingParameters, repositories);
                        task.fork();
                        taskList.add(task);
                    }
                });
                taskList.forEach(ForkJoinTask::join);
            } catch (InterruptedException | IOException exception) {
                logger.error("Операция прервана или время соединения истекло!");
            }
        }
        return new ArrayList<>(pagesSet);
    }

    public String getPath(String url, String siteUrl) {
        String path;
        if (url.equals(siteUrl)) {
            path = "/";
        } else {
            path = url.replace(siteUrl, "");
        }
        return path;
    }

    private boolean checkLink(String url, String siteUrl) {
        if(!url.startsWith(siteUrl)) {
            return false;
        }
        for (String rem : removal) {
            if (url.contains(rem)) {
                return false;
            }
        }
        return !linksSet.contains(url);
    }
}