package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@AllArgsConstructor
public class CollectorParameters {
    private SiteModel siteModel;
    private List<Page> pagesList;
    private CopyOnWriteArraySet<IndexModel> indexesSet;
    private ConcurrentHashMap<String, Lemma> lemmasMap;
}
