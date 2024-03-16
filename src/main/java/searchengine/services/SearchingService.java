package searchengine.services;

import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchResponse;

public interface SearchingService {
    SearchResponse getSearching(RequestParameters requestParam);
}
