package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.impl.IndexingServiceImpl;
import searchengine.services.impl.StatisticsServiceImpl;
import searchengine.services.impl.SearchingServiceImpl;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsServiceImpl statisticsService;
    private final IndexingServiceImpl indexingService;
    private final SearchingServiceImpl searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.getIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestBody String link) {
        return ResponseEntity.ok(indexingService.indexingPage(link));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam("query") String query,
                                                 @RequestParam("offset") int offset,
                                                 @RequestParam("limit") int limit,
                                                 @RequestParam(value = "site", required = false) String site) {
        return ResponseEntity.ok(searchingService.getSearching(new RequestParameters(query, site, offset, limit)));
    }
}