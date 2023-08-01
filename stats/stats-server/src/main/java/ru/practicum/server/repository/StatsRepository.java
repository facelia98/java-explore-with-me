package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT NEW ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT (h.ip)) FROM Hit AS h " +
            "WHERE h.created BETWEEN ?1 AND ?2 GROUP BY h.app, h.uri ORDER BY COUNT (h.ip) DESC")
    List<ViewStatsDto> findHitsByCreatedBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT (h.ip)) FROM Hit AS h " +
            "WHERE h.uri IN ?3 AND h.created BETWEEN ?1 AND ?2 GROUP BY h.app, h.uri ORDER BY COUNT (h.ip) DESC")
    List<ViewStatsDto> findHitsByCreatedBetweenAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT NEW ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT (DISTINCT h.ip)) FROM Hit AS h " +
            "WHERE h.created BETWEEN ?1 AND ?2 GROUP BY h.app, h.uri ORDER BY COUNT (h.ip) DESC")
    List<ViewStatsDto> findHitsByCreatedBetweenUnique(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT (DISTINCT h.ip)) FROM Hit AS h " +
            "WHERE h.uri IN ?3 AND h.created BETWEEN ?1 AND ?2 GROUP BY h.app, h.uri ORDER BY COUNT (h.ip) DESC")
    List<ViewStatsDto> findHitsByCreatedBetweenAndUriInUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT NEW ru.practicum.dto.ViewStatsDto(h.app, h.uri, COUNT (DISTINCT h.ip)) FROM Hit AS h " +
            "WHERE h.uri = ?1 GROUP BY h.app, h.uri ORDER BY COUNT (h.ip) DESC")
    List<ViewStatsDto> findHitsByUriUnique(String uri);


    @Query("select count(distinct h.ip) from Hit as h where h.uri = :uri")
    Integer findAllByUri(String uri);
}
