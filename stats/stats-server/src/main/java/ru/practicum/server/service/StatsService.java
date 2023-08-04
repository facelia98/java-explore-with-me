package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.exception.ValidationException;
import ru.practicum.server.mapper.HitMapper;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        if (end.isBefore(start) || start.isAfter(end)) {
            throw new ValidationException("Incoming date is invalid");
        }
        return (uris == null || uris.isEmpty()) ?
                unique ? statsRepository.findHitsByCreatedBetweenUnique(start, end) :
                        statsRepository.findHitsByCreatedBetween(start, end) :
                unique ? statsRepository.findHitsByCreatedBetweenAndUriInUnique(start, end, uris) :
                        statsRepository.findHitsByCreatedBetweenAndUriIn(start, end, uris);
    }

    @Transactional(readOnly = true)
    public Integer getViews(String uri) {
        return statsRepository.findAllByUri(uri);
    }

    @Transactional
    public void create(EndpointHitDto endpointHitDto) {
        statsRepository.save(HitMapper.toHit(endpointHitDto));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getViewsList(List<String> uris) {
        return statsRepository.findAllByUris(uris);
    }
}
