package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchResponseFalse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.impl.IndexingServiceImpl;
import searchengine.services.impl.StatisticsServiceImpl;
import searchengine.services.impl.SearchingServiceImpl;

import java.nio.charset.StandardCharsets;

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
    public ResponseEntity startIndexing() {
        if ((indexingService.getSiteParserService() == null || indexingService.getSiteParserService().isTerminated()) ||
                (indexingService.getLemmasAndIndexesParserPool() == null || indexingService.getLemmasAndIndexesParserPool().isTerminated())) {
            indexingService.getIndexing();
            return ResponseEntity.ok(new IndexingResponseTrue());
        } else {
            return ResponseEntity.ok(new IndexingResponseFalse("Индексация уже запущена"));
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        if ((indexingService.getSiteParserService() == null || indexingService.getSiteParserService().isTerminated()) ||
                (indexingService.getLemmasAndIndexesParserPool() == null || indexingService.getLemmasAndIndexesParserPool().isTerminated())) {
            return ResponseEntity.ok(new IndexingResponseFalse("Индексация не запущена"));
        } else {
            indexingService.stopIndexing();
            return ResponseEntity.ok(new IndexingResponseTrue());
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity indexPage(@RequestBody String link) {
        String decodeLink = java.net.URLDecoder.decode(link, StandardCharsets.UTF_8);
        String url = "url=";
        String result = decodeLink.substring(url.length());
        if (indexingService.checkLink(result)) {
            indexingService.indexingPage(result);
            return ResponseEntity.ok(new IndexingResponseTrue());
        } else {
            return ResponseEntity.ok(new IndexingResponseFalse(
                    "Данная страница находится за пределами сайтов," +
                            "указанных в конфигурационном файле"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam("query") String query,
                                 @RequestParam("offset") int offset,
                                 @RequestParam("limit") int limit,
                                 @RequestParam(value = "site", required = false) String site) {
        RequestParameters requestParam = new RequestParameters(query, site, offset, limit);
        if (query.isEmpty()) {
            return ResponseEntity.ok(new SearchResponseFalse("Задан пустой поисковый запрос."));
        }
        if (indexingService.getSiteParserService() != null) {
            if (indexingService.getSiteParserService().isTerminated()) {
                return ResponseEntity.ok(searchingService.getSearching(requestParam));
            } else {
                return ResponseEntity.ok(new SearchResponseFalse("Индексация ещё идёт. " +
                        "Дождитесь её завершения."));
            }
        } else {
            return ResponseEntity.ok(searchingService.getSearching(requestParam));
        }
    }
}