package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.ViewStatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateEventAdminRequest;
import ru.practicum.dto.updates.UpdateEventUserRequest;
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Event;
import ru.practicum.repositories.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.enums.Status.CONFIRMED;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestsRepository requestsRepository;
    private final LocationRepository locationRepository;
    private final ViewStatsClient client;


    public List<EventShortDto> get(Long userId, Integer from, Integer size) {
        log.info("GET Events request received");
        return eventRepository
                .findAllByInitiatorId(userId, PageRequest.of(from, size)).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getByIdAndInitiator(Long id, Long userId) {
        log.info("GET Event request received whit userId = {}, eventId = {}", userId, id);
        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        return EventMapper.toEventFullDto(eventRepository.findByInitiatorAndId(userRepository.getById(userId), id));
    }

    public EventFullDto getById(Long id, HttpServletRequest request) {
        log.info("GET Event request received whit eventId = {}", id);
        client.postHit(EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build());

        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        if (!eventRepository.getById(id).getEventState().equals("PUBLISHED")) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        Event event = eventRepository.getById(id);

        Integer tmp = client.getViews(request.getRequestURI());
        event.setViews(tmp.longValue());
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    public EventFullDto addNewEvent(NewEventDto dto, Long userId) {
        log.info("POST Event request received");
        if (!categoryRepository.existsById(dto.getCategory())) {
            log.error("Category not found for id = {}", dto.getCategory());
            throw new NotFoundException("Category not found for id = " + dto.getCategory());
        }
        if (!userRepository.existsById(userId)) {
            log.error("User not found for id = {}", userId);
            throw new NotFoundException("User not found for id = " + userId);
        }
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now())) {
                log.error("Event date should be after current date");
                throw new ValidationException("Event date should be after current date");
            }
        }
        if (dto.getPaid() == null) dto.setPaid(false);
        if (dto.getParticipantLimit() == null) dto.setParticipantLimit(0L);
        if (dto.getRequestModeration() == null) dto.setRequestModeration(true);
        Event returned = eventRepository
                .save(EventMapper.toEvent(dto,
                        categoryRepository.getById(dto.getCategory()),
                        userRepository.getById(userId),
                        locationRepository.save(dto.getLocation())));
        EventFullDto full = EventMapper.toEventFullDto(returned);
        return full;
    }

    public EventFullDto updateEvent(Long eventId, Long userId, UpdateEventUserRequest request) {
        log.info("PATCH Event request received for eventId = {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        }
        Event event = eventRepository.getById(eventId);
        if (event.getEventState().equals("PUBLISHED")) {
            log.error("Current event was published");
            throw new Conflict("Current event was published");
        }
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                log.error("Event date should be after current date");
                throw new ValidationException("Event date should be after current date");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategoryId() != null) event.setCategory(categoryRepository.getById(request.getCategoryId()));
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setEventState("PENDING");
            } else if (request.getStateAction().equals("CANCEL_REVIEW")) {
                event.setEventState("CANCELED");
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("PATCH ParticipationRequest request received for eventId = {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        }
        Event event = eventRepository.getById(eventId);
        if (request.getStateAction() != null && !event.getEventState().equals("PENDING")) {
            log.error("Event state is not PENDING");
            throw new Conflict("Event state is not PENDING");
        }
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                log.error("Event date should be after current date");
                throw new ValidationException("Event date should be after current date");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategoryId() != null) event.setCategory(categoryRepository.getById(request.getCategoryId()));
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                event.setEventState("PUBLISHED");
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                event.setEventState("CANCELED");
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    public List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime start, LocalDateTime end, Integer from, Integer size) {
        log.info("GET Events request received from admin endpoint");
        List<Event> tmp = eventRepository.findAllForAdmin(users, states, categories, PageRequest.of(from / size, size));
        LocalDateTime st = start == null ? LocalDateTime.MIN : start;
        LocalDateTime en = end == null ? LocalDateTime.MAX : end;
        return tmp.stream()
                .filter(event -> event.getEventDate().isAfter(st) && event.getEventDate().isBefore(en))
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String start,
                                         String end, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {
        log.info("GET Events request received");
        client.postHit(EndpointHitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("ewm-main-service")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build());

        String t = text == null ? " " : text;
        if (start != null && end != null) {
            if (LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isAfter(LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))) {
                log.warn("Validation exception: end-date is before start-date");
                throw new ValidationException("End-date is before start-date!");
            }
        }
        LocalDateTime st = start == null ? LocalDateTime.MIN : LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime en = end == null ? LocalDateTime.MAX : LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<EventShortDto> events = eventRepository.searchEvents(t, categoryIds, paid, "PUBLISHED",
                        PageRequest.of(from / size, size))
                .stream()
                .filter(event -> event.getEventDate().isAfter(st) && event.getEventDate().isBefore(en))
                .map(EventMapper::toEventShortDto)
                .map(this::setConfirmedRequests).collect(Collectors.toList());
        if (onlyAvailable) {
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
                    throw new ValidationException("Unallowed sorting method");
            }
        }
        return events;
    }

    private EventShortDto setConfirmedRequests(EventShortDto eventDto) {
        eventDto.setConfirmedRequests((long) requestsRepository.countParticipationRequests(eventDto.getId(),
                CONFIRMED.toString()).size());
        return eventDto;
    }
}