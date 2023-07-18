package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    List<ViewStatsDto> findHitsByCreatedBetween(LocalDateTime start, LocalDateTime end);

    List<ViewStatsDto> findHitsByCreatedBetweenAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

}
