package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class RequestParameters {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
