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
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Event;
import ru.practicum.repositories.*;

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

    public List<EventShortDto> get(Long userId, Integer from, Integer size) {
        return eventRepository
                .findAllByInitiatorId(userId, PageRequest.of(from, size)).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getByIdAndInitiator(Long id, Long userId) {
        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        return EventMapper.toEventFullDto(eventRepository.findByInitiatorAndId(userRepository.getById(userId), id));
    }

    public EventFullDto getById(Long id) {
        if (!eventRepository.existsById(id)) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        if (!eventRepository.getById(id).getEventState().equals("PUBLISHED")) {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        }
        return EventMapper.toEventFullDto(eventRepository.getById(id));
    }

    public EventFullDto addNewEvent(NewEventDto dto, Long userId) {
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
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        List<Event> tmp = eventRepository.findAllForAdmin(users, states, categories, rangeStart, rangeEnd, PageRequest.of(from / size, size));
        return tmp.stream().map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            if (LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isAfter(LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))) {
                log.warn("Validation exception: end-date is before start-date");
                throw new ValidationException("End-date is before start-date!");
            }
        }
        List<EventShortDto> events = eventRepository.searchEvents(text, categoryIds, paid, "PUBLISHED",
                        PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) :
                                event.getEventDate().isBefore(LocalDateTime.MAX))
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
                    throw new ValidationException("EventService: Сортировка возможна только по просмотрам или дате события.");
            }
        }
        return events;
    }

    private EventShortDto setConfirmedRequests(EventShortDto eventDto) {
        eventDto.setConfirmedRequests(Long.valueOf(requestsRepository.countParticipationRequests(eventDto.getId(),
                CONFIRMED.toString()).size()));
        return eventDto;
    }
}
