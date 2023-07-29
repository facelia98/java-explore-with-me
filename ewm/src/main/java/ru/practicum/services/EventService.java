package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateEventAdminRequest;
import ru.practicum.dto.updates.UpdateEventUserRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Event;
import ru.practicum.repositories.CategoryRepository;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.RequestsRepository;
import ru.practicum.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.enums.EventState.PUBLISHED;
import static ru.practicum.enums.Status.CONFIRMED;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestsRepository requestsRepository;

    public List<EventShortDto> get(Long userId, Integer from, Integer size) {
        return eventRepository
                .findAllByInitiator(userId, PageRequest.of(from, size)).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getByIdAndInitiator(Long id, Long userId) {
        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        return EventMapper.toEventFullDto(eventRepository.findByInitiatorAndId(userId, id));
    }

    public EventFullDto getById(Long id) {
        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        return EventMapper.toEventFullDto(eventRepository.getById(id));
    }

    public EventFullDto addNewEvent(NewEventDto dto, Long userId) {
        return EventMapper.toEventFullDto(eventRepository
                .save(EventMapper.toEvent(dto,
                        categoryRepository.getById(dto.getCategoryId()),
                        userRepository.getById(userId))));
    }

    public EventFullDto updateEvent(Long eventId, Long userId, UpdateEventUserRequest request) {
        if (!eventRepository.existsById(eventId)) {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        }
        Event event = eventRepository.getById(eventId);
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategoryId() != null) event.setCategory(categoryRepository.getById(request.getCategoryId()));
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getEventStateAction() != null) event.setEventState(request.getEventStateAction());

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        if (!eventRepository.existsById(eventId)) {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        }
        Event event = eventRepository.getById(eventId);
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategoryId() != null) event.setCategory(categoryRepository.getById(request.getCategoryId()));
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getEventStateAction() != null) event.setEventState(request.getEventStateAction());

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    public List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        return eventRepository.findAllForAdmin(users, states, categories, rangeStart, rangeEnd, PageRequest.of(from / size, size))
                .stream().map(event -> EventMapper.toEventFullDto(event))
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {

        List<EventShortDto> events = eventRepository.searchEvents(text, categoryIds, paid, PUBLISHED,
                        PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) :
                                event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toEventShortDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream().filter(shortEventDto ->
                    shortEventDto.getConfirmedRequests() < eventRepository
                            .findById(shortEventDto.getId()).get().getParticipantLimit() ||
                            eventRepository.findById(shortEventDto.getId()).get().getParticipantLimit() == 0
            ).collect(Collectors.toList());
        }
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(EventShortDto::getEventDate))
                            .collect(Collectors.toList());
                    break;
                case "VIEWS":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(EventShortDto::getViews))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new ValidationException("EventService: Сортировка возможна только по просмотрам или дате события.");
            }
        }
        return events;
    }

    private EventFullDto setConfirmedRequests(EventFullDto eventDto) {
        eventDto.setConfirmedRequests(requestsRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private EventShortDto setConfirmedRequests(EventShortDto eventDto) {
        eventDto.setConfirmedRequests(requestsRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }
}
