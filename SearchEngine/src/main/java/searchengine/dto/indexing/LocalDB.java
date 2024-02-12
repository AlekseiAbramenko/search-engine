package searchengine.dto.indexing;

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@AllArgsConstructor
public class LocalDB {
    private CopyOnWriteArraySet<IndexModel> indexesSet;
    private List<Lemma> lemmasList;
}
