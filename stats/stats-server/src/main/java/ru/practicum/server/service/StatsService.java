package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.mapper.HitMapper;
import ru.practicum.server.repository.StatsRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final StatsRepository statsRepository;

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        log.info("GET ViewStat request received to endpoint [/stats]");
        if (end.isBefore(start) || start.isAfter(end)) {
            throw new ValidationException("Incoming date is invalid");
        }
        return (uris == null || uris.isEmpty()) ?
                unique ? statsRepository.findHitsByCreatedBetweenUnique(start, end) :
                        statsRepository.findHitsByCreatedBetween(start, end) :
                unique ? statsRepository.findHitsByCreatedBetweenAndUriInUnique(start, end, uris) :
                        statsRepository.findHitsByCreatedBetweenAndUriIn(start, end, uris);
    }

    public void create(EndpointHitDto endpointHitDto) {
        log.info("POST EndpointHit request received to endpoint [/hit]");
        statsRepository.save(HitMapper.toHit(endpointHitDto));
    }
}
