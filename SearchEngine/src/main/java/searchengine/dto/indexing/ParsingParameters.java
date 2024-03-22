package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.concurrent.CopyOnWriteArraySet;

@Data
@AllArgsConstructor
public class ParsingParameters {
    private SiteModel siteModel;
    private String url;
    private CopyOnWriteArraySet<String> linksSet;
    private CopyOnWriteArraySet<Page> pagesSet;
}
