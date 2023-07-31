package ru.practicum.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.EventClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.services.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@Validated
public class EventController {
    private final EventService eventService;
    private final EventClient client;

    public EventController(EventService eventService, EventClient client) {
        this.eventService = eventService;
        this.client = client;
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categoryIds,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                         @Positive @RequestParam(defaultValue = "10") int size,
                                         HttpServletRequest request) {
        client.createHit(request);
        return eventService.getEvents(text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        client.createHit(request);
        return eventService.getById(id);
    }

}
