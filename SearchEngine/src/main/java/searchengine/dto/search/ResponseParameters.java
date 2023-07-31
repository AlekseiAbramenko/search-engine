package searchengine.dto.search;

import lombok.Data;

@Data
public class ResponseParameters {
    private String query;
    private String site;
    private int offset = 0;
    private int limit = 20;
}
