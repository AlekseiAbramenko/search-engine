package searchengine.dto.search;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SearchResponseFalse {
    private final boolean result = false;
    private final String error = "";
}