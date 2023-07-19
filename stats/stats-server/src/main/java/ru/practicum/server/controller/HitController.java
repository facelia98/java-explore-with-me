package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.service.StatsService;

import javax.validation.Valid;

@RestController
@RequestMapping("/hit")
@RequiredArgsConstructor
@Validated
public class HitController {
    private final StatsService statsService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        statsService.create(endpointHitDto);
    }
}
