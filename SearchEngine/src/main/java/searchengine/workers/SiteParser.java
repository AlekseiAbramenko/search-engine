package searchengine.workers;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.Connection;
import searchengine.dto.indexing.PageParameters;
import searchengine.model.Page;

import searchengine.model.SiteModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SiteParser extends RecursiveTask<List<Page>> {
    private List<SiteParser> taskList;
    private PageParameters pageParameters;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;
    private final Logger logger = LoggerFactory.getLogger(SiteParser.class);

    public SiteParser(PageParameters pageParameters, CopyOnWriteArraySet<String> linksSet,
                      CopyOnWriteArraySet<Page> pagesSet) {
        this.pageParameters = pageParameters;
        this.linksSet = linksSet;
        this.pagesSet = pagesSet;
    }

    public SiteParser() {
    }

    @Override
    public List<Page> compute() {
        if (getPool().isShutdown() && taskList != null) {
            taskList.clear();
        } else {
            try {
                String url = pageParameters.getUrl();
                SiteModel siteModel = pageParameters.getSiteModel();
                taskList = new ArrayList<>();
                Document doc = Connection.getConnection(url);
                Thread.sleep(500);
                int code = doc.connection().response().statusCode();
                String content = doc.html();
                String path = getPath(url, siteModel.getUrl());
                if(code == 200) {
                    PageParameters pageParam = new PageParameters(siteModel, path, content, code);
                    addPage(pageParam);
                }
                Elements elements = doc.select("a[href]");
                elements.forEach(element -> {
                    String link = element.absUrl("href");
                    if (checkLink(link, siteModel.getUrl())) {
                        linksSet.add(link);
                        PageParameters newPageParam = new PageParameters(siteModel, link);
                        SiteParser task = new SiteParser(newPageParam, linksSet, pagesSet);
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

    private void addPage(PageParameters pageParam) {
        Page page = new Page();
        page.setSite(pageParam.getSiteModel());
        page.setCode(pageParam.getCod());
        page.setPath(pageParam.getUrl());
        page.setContent(pageParam.getContent());
        pagesSet.add(page);
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
        return url.startsWith(siteUrl)
                && !linksSet.contains(url)
                && !url.contains("#")
                && !url.contains("?utm")
                && !url.contains(".pdf")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".php");
    }
}