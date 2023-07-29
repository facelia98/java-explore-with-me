package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateEventUserRequest;
import ru.practicum.models.EventRequestStatusUpdateRequest;
import ru.practicum.models.EventRequestStatusUpdateResult;
import ru.practicum.services.EventService;
import ru.practicum.services.RequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEvents(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                         @RequestParam(name = "userId") Long userId) {
        return eventService.get(userId, from, size);
    }

    @PostMapping("/{userId}/events")
    public EventFullDto getEvents(@RequestParam(name = "userId") Long userId,
                                  @RequestBody NewEventDto dto) {
        return eventService.addNewEvent(dto, userId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventById(@RequestParam(name = "userId") Long userId,
                                     @RequestParam(name = "eventId") Long id) {
        return eventService.getByIdAndInitiator(id, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@RequestParam(name = "userId") Long userId,
                                    @RequestParam(name = "eventId") Long id,
                                    @RequestBody UpdateEventUserRequest request) {
        return eventService.updateEvent(id, userId, request);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUser(@RequestParam(name = "userId") Long userId,
                                                            @RequestParam(name = "eventId") Long eventId) {
        return requestService.getRequestsForUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult requestStatusUpdate(@RequestParam(name = "userId") Long userId,
                                                              @RequestParam(name = "eventId") Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        return requestService.requestStatusUpdate(userId, eventId, request);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ParticipationRequestDto saveParticipationRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return requestService.saveParticipationRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable Long userId) {
        return requestService.getParticipationRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId,
                                                              @PathVariable Long requestId) {
        return requestService.cancelParticipationRequest(userId, requestId);
    }

}
