package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

import java.util.concurrent.ExecutorService;

public interface IndexingService {
    IndexingResponse getIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexingPage(String link);
    ExecutorService getSiteParserService();
    boolean checkLink(String path);
}
