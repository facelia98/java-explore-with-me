package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateCommentDto;
import ru.practicum.dto.updates.UpdateEventRequest;
import ru.practicum.models.EventRequestStatusUpdateRequest;
import ru.practicum.models.EventRequestStatusUpdateResult;
import ru.practicum.services.interfaces.CommentService;
import ru.practicum.services.interfaces.EventService;
import ru.practicum.services.interfaces.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEvents(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size,
                                         @PathVariable Long userId) {
        log.info("GET Events request received to user endpoint with userId = {}", userId);
        return eventService.get(userId, from, size);
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto getEvents(@PathVariable Long userId,
                                  @Valid @RequestBody NewEventDto dto) {
        log.info("POST Event request received to user endpoint with userId = {}", userId);
        return eventService.addNewEvent(dto, userId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        log.info("GET Event request received to user endpoint with userId = {}, eventId = {}", userId, eventId);
        return eventService.getByIdAndInitiator(eventId, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventRequest request) {
        log.info("PATCH Event request received to user endpoint with userId = {}, eventId = {}", userId, eventId);
        return eventService.updateEvent(eventId, userId, request);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUser(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        log.info("GET ParticipationRequest request received to user endpoint with userId = {}, eventId = {}", userId, eventId);
        return requestService.getRequestsForUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult requestStatusUpdate(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH ParticipationRequest request received to user endpoint with userId = {}, eventId = {}", userId, eventId);
        return requestService.requestStatusUpdate(userId, eventId, request);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ParticipationRequestDto saveParticipationRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("POST ParticipationRequest request received to user endpoint with userId = {}, eventId = {}", userId, eventId);
        return requestService.saveParticipationRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable Long userId) {
        log.info("GET ParticipationRequests request received to user endpoint with userId = {}", userId);
        return requestService.getParticipationRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId,
                                                              @PathVariable Long requestId) {
        log.info("PATCH CANCEL ParticipationRequest request received for requestId = {}, userId = {}", requestId, userId);
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @PostMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @RequestBody @Valid NewCommentDto dto) {
        log.info("POST Comment request received to user endpoint with eventId = {}, userId = {}", eventId, userId);
        return commentService.saveComment(userId, eventId, dto);
    }

    @PatchMapping("/{userId}/events/{eventId}/comments/{commentId}")
    public CommentShortDto updateComment(@PathVariable Long commentId,
                                         @PathVariable Long userId,
                                         @RequestBody @Valid UpdateCommentDto commentDto) {
        log.info("PATCH Comment request received to user endpoint with commentId = {}, userId = {}", commentId, userId);
        return commentService.updateCommentUser(commentId, userId, commentDto);
    }

    @DeleteMapping("/{userId}/events/{eventId}/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void userDeleteComment(@PathVariable Long userId,
                                  @PathVariable Long commentId) {
        log.info("DELETE Comment request received to user endpoint with commentId = {}, userId = {}", commentId, userId);
        commentService.deleteCommentUser(commentId, userId);
    }

    @GetMapping("/{userId}/events/{eventId}/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("GET Comments request received to user endpoint with eventId = {}", commentId);
        return commentService.getById(commentId);
    }
}
