package ru.practicum.mappers;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.models.ParticipationRequest;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder().id(participationRequest.getId())
                .requester(participationRequest.getId())
                .event(participationRequest.getEvent().getId())
                .created(participationRequest.getCreated())
                .status(participationRequest.getStatus())
                .build();
    }
}
