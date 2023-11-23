package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.dto.search.RequestParameters;
import searchengine.dto.search.SearchResponseFalse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.impl.IndexingService;
import searchengine.services.impl.StatisticsService;
import searchengine.services.impl.SearchingService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchingService searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        if(indexingService.getService() == null || indexingService.getService().isTerminated()){
            indexingService.getIndexing();
            return ResponseEntity.ok(new IndexingResponseTrue());
        } else {
            return ResponseEntity.ok(new IndexingResponseFalse("Индексация уже запущена"));
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        if(indexingService.getService() == null || indexingService.getService().isTerminated()) {
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
        if(indexingService.checkLink(result)) {
            indexingService.indexingPage(result);
            return ResponseEntity.ok(new IndexingResponseTrue());
        } else {
            return ResponseEntity.ok(new IndexingResponseFalse(
                    "Данная страница находится за пределами сайтов," +
                            "указанных в конфигурационном файле"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam ("query") String query,
                                 @RequestParam ("offset") int offset,
                                 @RequestParam ("limit") int limit,
                                 @RequestParam (value = "site", required = false) String site) {
        RequestParameters requestParam = new RequestParameters(query, site, offset, limit);
        if(query.length() == 0) {
            return ResponseEntity.ok(new SearchResponseFalse("Задан пустой поисковый запрос"));
        } else {
            return ResponseEntity.ok(searchingService.getSearching(requestParam));
        }
        //todo: сюда добавить понятных ответов на возможные ошибки через switch case,
        // например, если индексация ещё идет
        // или не найдено ни одной страницы (проверить заведомо отсутствующим словом)
    }
}