package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

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

        return ResponseEntity.ok(new IndexingResponseTrue());//заменить
    }
}