package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.ViewStatsClient;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.GetEventParametersDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateEventRequest;
import ru.practicum.enums.Status;
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Category;
import ru.practicum.models.Event;
import ru.practicum.models.User;
import ru.practicum.repositories.*;
import ru.practicum.services.interfaces.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.enums.Status.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestsRepository requestsRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final ViewStatsClient client;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> get(Long userId, Integer from, Integer size) {
        List<EventShortDto> tmp = eventRepository
                .findAllByInitiatorId(userId, PageRequest.of(from, size)).stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, (long) getCommentListForEvent(event.getId()).size()))
                .collect(Collectors.toList());
        List<String> idsToViews = tmp.stream()
                .map(eventShortDto -> "/events/" + eventShortDto.getId()).collect(Collectors.toList());
        Map<String, Long> views = client.getViewsForList(idsToViews);
        tmp.stream().forEach(eventShortDto -> eventShortDto.setViews(views.getOrDefault("/events/" + eventShortDto.getId(), 0L)));
        return tmp;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByIdAndInitiator(Long eventId, Long userId) {
        if (!eventRepository.existsById(eventId)) {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found for id = {}", userId);
            throw new NotFoundException("User not found for id = " + userId);
        });
        return EventMapper.toEventFullDto(eventRepository.findByInitiatorAndId(user, eventId),
                client.getViews("/events/" + eventId).longValue(), getCommentListForEvent(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id).orElseThrow(() -> {
            log.error("Event not found for id = {}", id);
            throw new NotFoundException("Event not found for id = " + id);
        });

        if (!event.getEventState().equals(PUBLISHED)) {
            log.error("Published event not found for id = {}", id);
            throw new NotFoundException("Published event not found for id = " + id);
        }

        return EventMapper.toEventFullDto(eventRepository.save(event),
                client.getViews("/events/" + event.getId()).longValue(), getCommentListForEvent(id));
    }

    @Override
    @Transactional
    public EventFullDto addNewEvent(NewEventDto dto, Long userId) {
        Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(() -> {
            log.error("Category not found for id = {}", dto.getCategory());
            throw new NotFoundException("Category not found for id = " + dto.getCategory());
        });

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found for id = {}", userId);
            throw new NotFoundException("User not found for id = " + userId);
        });

        if (dto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            log.error("Event date should be no earlier than 2 hours later");
            throw new ValidationException("Event date should be no earlier than 2 hours later");
        }

        if (dto.getPaid() == null) dto.setPaid(false);
        if (dto.getParticipantLimit() == null) dto.setParticipantLimit(0L);
        if (dto.getRequestModeration() == null) dto.setRequestModeration(true);
        return EventMapper.toEventFullDto(eventRepository.save(EventMapper.toEvent(dto, category, user,
                locationRepository.save(dto.getLocation()))), 0L, null);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, Long userId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        });

        if (event.getEventState().equals(PUBLISHED)) {
            log.error("Current event was published");
            throw new Conflict("Current event was published");
        }

        updateToEventEntityMapper(request, event);

        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setEventState(Status.PENDING);
            } else if (request.getStateAction().equals("CANCEL_REVIEW")) {
                event.setEventState(Status.CANCELED);
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(event),
                client.getViews("/events/" + eventId).longValue(),
                getCommentListForEvent(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        });

        if (request.getStateAction() != null && !event.getEventState().equals(PENDING)) {
            log.error("Event state is not PENDING");
            throw new Conflict("Event state is not PENDING");
        }

        updateToEventEntityMapper(request, event);

        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                event.setEventState(Status.PUBLISHED);
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                event.setEventState(Status.CANCELED);
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(event),
                client.getViews("/events/" + eventId).longValue(),
                getCommentListForEvent(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsAdmin(GetEventParametersDto dto) {
        LocalDateTime st = dto.getRangeStart() == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : dto.getRangeStart();
        LocalDateTime en = dto.getRangeEnd() == null ? LocalDateTime.now().plusYears(30) : dto.getRangeEnd();
        List<Event> events = eventRepository.findAllForAdmin(dto.getUsers(), dto.getStates(), dto.getCategories(), st, en,
                PageRequest.of(dto.getFrom() / dto.getSize(), dto.getSize()));
        List<String> idsToViews = events.stream()
                .map(event -> "/events/" + event.getId()).collect(Collectors.toList());
        Map<String, Long> views = client.getViewsForList(idsToViews);

        List<EventFullDto> temp = events.stream()
                .map(event -> EventMapper.toEventFullDto(event, 0L, getCommentListForEvent(event.getId())))
                .collect(Collectors.toList());
        temp.stream().forEach(eventFullDto -> eventFullDto.setViews(views.getOrDefault("/events/" + eventFullDto.getId(), 0L)));
        return temp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(GetEventParametersDto dto, HttpServletRequest request) {
        String t = dto.getText() == null ? " " : dto.getText();
        if (dto.getRangeStart() != null && dto.getRangeEnd() != null) {
            if (dto.getRangeStart().isAfter(dto.getRangeEnd())) {
                log.warn("Validation exception: end-date is before start-date");
                throw new ValidationException("End-date is before start-date!");
            }
        }
        LocalDateTime st = dto.getRangeStart() == null ? LocalDateTime.MIN : dto.getRangeStart();
        LocalDateTime en = dto.getRangeEnd() == null ? LocalDateTime.MAX : dto.getRangeEnd();
        List<EventShortDto> events = eventRepository.searchEvents(t, dto.getCategoryIds(), dto.getPaid(), PUBLISHED,
                        PageRequest.of(dto.getFrom() / dto.getSize(), dto.getSize()))
                .stream()
                .filter(event -> event.getEventDate().isAfter(st) && event.getEventDate().isBefore(en))
                .map(event -> EventMapper.toEventShortDto(event, 0L,
                        (long) getCommentListForEvent(event.getId()).size()))
                .map(this::setConfirmedRequests).collect(Collectors.toList());
        List<String> idsToViews = events.stream()
                .map(eventShortDto -> "/events/" + eventShortDto.getId()).collect(Collectors.toList());
        Map<String, Long> views = client.getViewsForList(idsToViews);
        events.stream().forEach(eventShortDto -> eventShortDto.setViews(views.getOrDefault("/events/" + eventShortDto.getId(), 0L)));
        if (dto.getOnlyAvailable()) {
            events = events.stream().filter(shortEventDto ->
                    shortEventDto.getConfirmedRequests() < eventRepository
                            .findById(shortEventDto.getId()).get().getParticipantLimit() ||
                            eventRepository.findById(shortEventDto.getId()).get().getParticipantLimit() == 0
            ).collect(Collectors.toList());
        }
        if (!(dto.getSort().equals("EVENT_DATE") || dto.getSort().equals("VIEWS")))
            throw new ValidationException("Unallowed sorting method!");

        return events.stream().sorted(dto.getSort().equals("EVENT_DATE") ?
                        Comparator.comparing(EventShortDto::getEventDate) :
                        Comparator.comparing(EventShortDto::getViews))
                .collect(Collectors.toList());
    }

    private EventShortDto setConfirmedRequests(EventShortDto eventDto) {
        eventDto.setConfirmedRequests((long) requestsRepository.countParticipationRequests(eventDto.getId(),
                CONFIRMED).size());
        return eventDto;
    }

    private void updateToEventEntityMapper(UpdateEventRequest request, Event event) {
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getEventDate() != null) {
            if (request.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
                log.error("Event date should be no earlier than 2 hours later");
                throw new ValidationException("Event date should be no earlier than 2 hours later");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategoryId() != null) event.setCategory(categoryRepository.getById(request.getCategoryId()));
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
    }

    private List<CommentShortDto> getCommentListForEvent(Long eventId) {
        return commentRepository.findAllByEvent_Id(eventId)
                .stream().map(CommentMapper::toCommentShortDto).collect(Collectors.toList());
    }
}
