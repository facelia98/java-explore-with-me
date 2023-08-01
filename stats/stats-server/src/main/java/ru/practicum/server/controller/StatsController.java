package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;


    @GetMapping("/views")
    public Integer getViews(@RequestParam(name = "uri", required = false) String uri) {
        return statsService.getViews(uri);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(name = "unique", defaultValue = "false") Boolean unique,
                                       @RequestParam(name = "uris", required = false) List<String> uris) {
        return statsService.getStats(start, end, unique, uris);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void createHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        statsService.create(endpointHitDto);
    }
}
