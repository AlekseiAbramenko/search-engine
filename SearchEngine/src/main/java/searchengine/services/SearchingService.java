package searchengine.services;

import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchResponseTrue;

public interface SearchingService {
    SearchResponseTrue getSearching(RequestParameters requestParam);
}
