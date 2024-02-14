package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.concurrent.CopyOnWriteArraySet;

@Data
public class PageParameters {
    private SiteModel siteModel;
    private String url;
    private String content;
    private int cod;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;


    public PageParameters(SiteModel siteModel, String url, CopyOnWriteArraySet<String> linksSet, CopyOnWriteArraySet<Page> pagesSet) {
        this.siteModel = siteModel;
        this.url = url;
        this.linksSet = linksSet;
        this.pagesSet = pagesSet;
    }

    public PageParameters(SiteModel siteModel, String url, String content, int cod) {
        this.siteModel = siteModel;
        this.url = url;
        this.content = content;
        this.cod = cod;
    }
}
