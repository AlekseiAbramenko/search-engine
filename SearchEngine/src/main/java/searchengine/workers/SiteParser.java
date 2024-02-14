package searchengine.workers;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.config.Connection;
import searchengine.config.Repositories;
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
    private Repositories repositories;
    private List<SiteParser> taskList;
    private PageParameters parsingParameters;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;
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
                    PageParameters addPageParameters = new PageParameters(siteModel, path, content, code);
                    addPage(addPageParameters);
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

    private synchronized void addPage(PageParameters addPageParameters) {
        Page page = new Page();
        SiteModel siteModel = addPageParameters.getSiteModel();
        page.setSite(siteModel);
        page.setCode(addPageParameters.getCod());
        page.setPath(addPageParameters.getUrl());
        page.setContent(addPageParameters.getContent());
        pagesSet.add(page);
//        siteModel.setStatusTime(LocalDateTime.now());
//        repositories.getSiteRepository().save(siteModel);
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
                && !url.contains(".PDF")
                && !url.contains(".png")
                && !url.contains(".jpg")
                && !url.contains(".jpeg")
                && !url.contains(".JPG")
                && !url.contains(".xlsx")
                && !url.contains(".doc")
                && !url.contains(".eps")
                && !url.contains(".php");
    }
}