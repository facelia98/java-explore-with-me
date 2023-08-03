package ru.practicum.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.models.Event;
import ru.practicum.models.User;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.id IN :events " +
            "ORDER BY e.id")
    Set<Event> findEventsByIds(List<Long> events);

    Event findByInitiatorAndId(User user, Long id);

    List<Event> findByCategoryId(Long categoryId);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.eventState IN :states) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories)")
    List<Event> findAllForAdmin(List<Long> users, List<String> states, List<Long> categories, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (lower(e.annotation) like lower(concat('%', :text, '%')) " +
            "OR lower(e.description) like lower(concat('%', :text, '%'))) " +
            "AND ((:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "AND ((:paid) IS NULL OR e.paid = :paid)" +
            "AND e.eventState IN :state")
    List<Event> searchEvents(String text, List<Long> categoryIds, Boolean paid, String state, Pageable pageable);
}