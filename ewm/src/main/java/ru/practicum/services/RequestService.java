package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.Status;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.RequestMapper;
import ru.practicum.models.*;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.RequestsRepository;
import ru.practicum.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    private final RequestsRepository requestsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getRequestsForUser(Long userId, Long eventId) {
        return requestsRepository.getParticipationRequestByEventAndRequester(eventId, userId)
                .stream().map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult requestStatusUpdate(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        for (int i = 0; i < request.getRequestIds().size(); i++) {
            ParticipationRequest participationRequest = requestsRepository.getById(request.getRequestIds().get(i));
            participationRequest.setStatus(request.getStatus());
            if (request.getStatus() == Status.CONFIRMED) {
                result.getConfirmedRequests().add(participationRequest);
            } else {
                result.getRejectedRequests().add(participationRequest);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequests(Long userId) {
        return requestsRepository.findAllByRequesterId(userId)
                .stream()
                .map(participationRequest -> RequestMapper.toParticipationRequestDto(participationRequest))
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestsRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new ValidationException(""));
        request.setStatus(Status.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestsRepository.save(request));
    }

    public ParticipationRequestDto saveParticipationRequest(Long userId, Long eventId) {
        if (requestsRepository.findByEventIdAndRequesterId(eventId, userId) != null) {
            throw new ValidationException("Duplicate exception");
        }
        User user = userRepository.getById(userId);
        Event event = eventRepository.getById(eventId);

        if (userId.equals(event.getInitiator().getId())) {
            throw new ValidationException("Initiator couldn't send request");
        }
        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Unpublished event");
        }
        if (event.getParticipantLimit() <= requestsRepository
                .countParticipationByEventIdAndStatus(eventId, Status.CONFIRMED)) {
            throw new ValidationException("Limit");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);

        if (Boolean.TRUE.equals(event.getRequestModeration())) {
            request.setStatus(Status.PENDING);
        } else {
            request.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }
        return RequestMapper.toParticipationRequestDto(requestsRepository.save(request));
    }
}
