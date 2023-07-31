package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchResponseTrue {
    private boolean result = true;
    private int count;
    private DataModel[] data;
}
