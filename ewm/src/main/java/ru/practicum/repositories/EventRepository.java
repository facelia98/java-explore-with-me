package ru.practicum.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.EventState;
import ru.practicum.models.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiator(Long userId, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.id IN :events " +
            "ORDER BY e.id")
    List<Event> findEventsByIds(List<Long> events);

    Event findByInitiatorAndId(Long userId, Long id);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.eventDate BETWEEN :start AND :end " +
            "AND ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.eventState IN :states) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories)")
    List<Event> findAllForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (lower(e.annotation) like lower(concat('%', :text, '%')) " +
            "OR lower(e.description) like lower(concat('%', :text, '%'))) " +
            "AND ((:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "AND e.paid = :paid " +
            "AND e.eventState IN :state")
    Page<Event> searchEvents(String text, List<Long> categoryIds, Boolean paid, EventState state, Pageable pageable);
}
