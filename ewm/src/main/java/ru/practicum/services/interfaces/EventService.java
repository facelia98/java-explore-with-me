package ru.practicum.services.interfaces;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.GetEventParametersDto;
import ru.practicum.dto.news.NewEventDto;
import ru.practicum.dto.updates.UpdateEventRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    List<EventShortDto> get(Long userId, Integer from, Integer size);

    EventFullDto getByIdAndInitiator(Long id, Long userId);

    EventFullDto getById(Long id, HttpServletRequest request);

    EventFullDto addNewEvent(NewEventDto dto, Long userId);

    EventFullDto updateEvent(Long eventId, Long userId, UpdateEventRequest request);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest request);

    List<EventFullDto> getEventsAdmin(GetEventParametersDto dto);

    List<EventShortDto> getEvents(GetEventParametersDto dto, HttpServletRequest request);

}