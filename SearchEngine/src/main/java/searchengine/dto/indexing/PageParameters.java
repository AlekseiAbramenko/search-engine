package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.SiteModel;

@Data
@AllArgsConstructor
public class PageParameters {
    private SiteModel siteModel;
    private String url;
    private String content;
    private int cod;

    public PageParameters(SiteModel siteModel, String url) {
        this.siteModel = siteModel;
        this.url = url;
    }
}
