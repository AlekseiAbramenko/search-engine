package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.SiteModel;

@Data
@AllArgsConstructor
public class ParsingParameters {
    private SiteModel siteModel;
    private String url;
}
