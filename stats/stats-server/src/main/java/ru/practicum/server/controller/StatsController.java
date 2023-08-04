package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    private final StatsService statsService;


    @GetMapping("/views/list")
    public Map<String, List<Long>> getViews(@RequestParam(name = "uris", required = false) List<String> uris) {
        log.info("GET views list count request received to endpoint [/stats], uris = {}", uris);
        return statsService.getViewsList(uris);
    }

    @GetMapping("/views")
    public Integer getViews(@RequestParam(name = "uri", required = false) String uri) {
        log.info("GET views count request received to endpoint [/stats]");
        return statsService.getViews(uri);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(name = "unique", defaultValue = "false") Boolean unique,
                                       @RequestParam(name = "uris", required = false) List<String> uris) {
        log.info("GET ViewStat request received to endpoint [/stats]");
        return statsService.getStats(start, end, unique, uris);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void createHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        log.info("POST EndpointHit request received to endpoint [/hit]");
        statsService.create(endpointHitDto);
    }
}
