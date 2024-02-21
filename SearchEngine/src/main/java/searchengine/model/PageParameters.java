package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.concurrent.CopyOnWriteArraySet;

@Data
@AllArgsConstructor
public class PageParameters {
    private SiteModel siteModel;
    private String url;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;
}
