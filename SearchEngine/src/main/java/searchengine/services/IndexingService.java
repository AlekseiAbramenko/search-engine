package searchengine.services;

import java.util.concurrent.ExecutorService;

public interface IndexingService {
    void getIndexing();
    void stopIndexing();
    void indexingPage(String link);
    ExecutorService getService();
    boolean checkLink(String path);

}
