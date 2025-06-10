package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.requests.UrlRequest;
import searchengine.dto.responses.SuccessResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Метод возвращает статистику и другую служебную информацию о
     * состоянии поисковых индексов и самого движка.
     * @return {@code ResponseEntity<StatisticsResponse>}
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ok(statisticsService.getStatistics());
    }

    /**
     * Метод запускает полную индексацию всех сайтов или полную
     * переиндексацию, если они уже проиндексированы.
     * Если в настоящий момент индексация или переиндексация уже
     * запущена, метод возвращает соответствующее сообщение об ошибке.
     * @return {@code ResponseEntity<SuccessResponse>}
     */
    @GetMapping("/startIndexing")
    public ResponseEntity<SuccessResponse> startIndexing() {
        return ok(new SuccessResponse());
    }

    /**
     * Метод останавливает текущий процесс индексации (переиндексации).
     * Если в настоящий момент индексация или переиндексация не происходит,
     * метод возвращает соответствующее сообщение об ошибке.
     * @return {@code ResponseEntity<SuccessResponse>}
     */
    @GetMapping("/stopIndexing")
    public ResponseEntity<SuccessResponse> stopIndexing() {
        return ok(new SuccessResponse());
    }

    /**
     * Метод добавляет в индекс или обновляет отдельную страницу, адрес
     * которой передан в параметре.
     * Если адрес страницы передан неверно, метод должен вернуть
     * соответствующую ошибку.
     * @param url адрес страницы, которую нужно переиндексировать.
     * @return {@code ResponseEntity<SuccessResponse>}
     */
    @PostMapping("/indexPage")
    public ResponseEntity<SuccessResponse> indexPage(@RequestBody UrlRequest url) {
        return ok(new SuccessResponse());
    }

    /**
     * Метод осуществляет поиск страниц по переданному поисковому запросу
     * @param query поисковый запрос
     * @param site сайт по которому осуществлять поиск
     * @param offset сдвиг от 0 для постраничного вывода
     * @param limit количество результатов, которое необходимо вывести
     */
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse> search(@RequestParam("query") String query,
                                                  @RequestParam(value = "site", required = false) Optional<String> site,
                                                  @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                  @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        return ok(new SuccessResponse());
    }
}
