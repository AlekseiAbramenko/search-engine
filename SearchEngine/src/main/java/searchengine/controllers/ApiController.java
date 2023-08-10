package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.dto.search.SearchResponseFalse;
import searchengine.dto.search.SearchResponseTrue;
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
    public ResponseEntity search(@RequestBody String query) {
        //todo: добавить параметры: site, offset, limit
//        System.out.println(query);
        return ResponseEntity.ok(searchingService.getSearching(query));
    }
}