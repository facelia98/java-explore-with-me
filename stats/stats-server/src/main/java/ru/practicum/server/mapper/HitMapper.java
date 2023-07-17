package ru.practicum.server.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {
    public static EndpointHitDto toEndpointHitDto(Hit hit) {
        return EndpointHitDto.builder()
                .app(hit.getApp())
                .ip(hit.getIp())
                .id(hit.getId())
                .timestamp(hit.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .uri(hit.getUri())
                .build();

    }

    public static Hit toHit(EndpointHitDto endpointHitDto) {
        return Hit.builder()
                .app(endpointHitDto.getApp())
                .ip(endpointHitDto.getIp())
                .uri(endpointHitDto.getUri())
                .created(LocalDateTime.parse(endpointHitDto.getTimestamp(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
