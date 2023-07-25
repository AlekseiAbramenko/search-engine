package searchengine.dto.indexing;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class IndexingResponseFalse {
    private final boolean result = false;
    private final String error;
}