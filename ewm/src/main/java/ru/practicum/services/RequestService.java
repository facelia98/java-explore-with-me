package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.enums.Status;
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.models.*;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.RequestsRepository;
import ru.practicum.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    private final RequestsRepository requestsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForUser(Long userId, Long eventId) {
        log.info("GET ParticipationRequests request received for eventId = {}", eventId);
        return requestsRepository.findAllByEvent_Id(eventId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult requestStatusUpdate(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        log.info("PATCH ParticipationRequest request received for eventId = {}", eventId);
        Event event = eventRepository.getById(eventId);

        List<Long> ids = request.getRequestIds();
        Status state = request.getStatus();

        List<ParticipationRequestDto> confirmedList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedList = new ArrayList<>();

        for (Long id : ids) {

            if (event.getParticipantLimit() != 0L) {
                int count = requestsRepository
                        .countParticipationRequests(eventId, "CONFIRMED").size();
                if (event.getParticipantLimit() <= count) {
                    throw new Conflict("Limit");
                }
            }

            ParticipationRequest newrequest = requestsRepository.findByIdAndEvent_Id(id, eventId).orElseThrow(() ->
                    new NotFoundException("PR not found"));

            if (!newrequest.getStatus().equals("PENDING")) {
                throw new Conflict("Conflict");
            }
            if (state.equals(Status.CONFIRMED)) {
                newrequest.setStatus("CONFIRMED");
                confirmedList.add(RequestMapper.toParticipationRequestDto(requestsRepository.save(newrequest)));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else {
                newrequest.setStatus("REJECTED");
                rejectedList.add(RequestMapper.toParticipationRequestDto(requestsRepository.save(newrequest)));
            }
        }
        eventRepository.save(event);
        return new EventRequestStatusUpdateResult(confirmedList, rejectedList);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        log.info("GET ParticipationRequest request received for userId = {}", userId);
        return requestsRepository.findAllByRequesterId(userId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        log.info("PATCH CANCEL ParticipationRequest request received for requestId = {}, userId = {}", requestId, userId);
        ParticipationRequest request = requestsRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new ValidationException("ParticipationRequest not found for pair userId-requestId"));
        request.setStatus(Status.CANCELED.toString());
        return RequestMapper.toParticipationRequestDto(requestsRepository.save(request));
    }

    @Transactional
    public ParticipationRequestDto saveParticipationRequest(Long userId, Long eventId) {
        log.info("POST ParticipationRequest request received for eventId = {}, userId = {}", eventId, userId);
        List<ParticipationRequest> pr = requestsRepository.findByEventIdAndRequesterId(eventId, userId);
        if (!pr.isEmpty()) {
            throw new Conflict("Duplicate participation request");
        }
        User user = userRepository.getById(userId);
        Event event = eventRepository.getById(eventId);

        if (userId.equals(event.getInitiator().getId())) {
            throw new Conflict("Initiator couldn't send request");
        }
        if (!event.getEventState().equals("PUBLISHED")) {
            throw new Conflict("Unpublished event");
        }
        if (event.getParticipantLimit() != 0L) {
            int count = requestsRepository
                    .countParticipationRequests(eventId, "CONFIRMED").size();
            if (event.getParticipantLimit() <= count) {
                throw new Conflict("Limit");
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);

        if (event.getRequestModeration() && event.getParticipantLimit() != 0L) {
            request.setStatus("PENDING");
        } else {
            request.setStatus("CONFIRMED");
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        ParticipationRequest returned = requestsRepository.save(request);
        return RequestMapper.toParticipationRequestDto(returned);
    }
}