package ru.practicum.services.interfaces;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.models.EventRequestStatusUpdateRequest;
import ru.practicum.models.EventRequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getRequestsForUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult requestStatusUpdate(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getParticipationRequests(Long userId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    ParticipationRequestDto saveParticipationRequest(Long userId, Long eventId);

}