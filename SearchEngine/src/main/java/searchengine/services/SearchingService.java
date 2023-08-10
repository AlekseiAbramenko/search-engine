package searchengine.services;

import searchengine.dto.search.SearchResponseTrue;

public interface SearchingService {
    SearchResponseTrue getSearching(String query);
}
