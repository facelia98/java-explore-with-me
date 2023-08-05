package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.ViewStatsClient;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Compilation;
import ru.practicum.models.Event;
import ru.practicum.repositories.CompilationRepository;
import ru.practicum.repositories.EventRepository;
import ru.practicum.services.interfaces.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ViewStatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> get(Boolean pinned, Integer from, Integer size) {
        if (pinned != null) {
            return compilationRepository.findAll(PageRequest.of(from, size)).get()
                    .filter(compilation -> compilation.getPinned().equals(pinned))
                    .map(compilation -> CompilationMapper.toCompilationDto(compilation,
                            compilation.getEvents().stream().map(this::toShortEventDtoWithViews).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(PageRequest.of(from, size)).get()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation,
                        compilation.getEvents().stream().map(this::toShortEventDtoWithViews).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long id) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(id).orElseThrow(() -> {
            log.error("Compilation not found for id = {}", id);
            throw new NotFoundException("Compilation not found for id = " + id);
        }), compilationRepository.getById(id).getEvents()
                .stream().map(this::toShortEventDtoWithViews).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long id) {
        if (!compilationRepository.existsById(id)) {
            log.error("Compilation not found for id = {}", id);
            throw new NotFoundException("Incorrect id");
        }
        compilationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long id, UpdateCompilationRequest compilationUpdateDto) {
        Compilation compilationToUpdate = compilationRepository.findById(id).orElseThrow(() -> {
            log.error("Compilation not found for id = {}", id);
            throw new NotFoundException("Compilation not found for id = " + id);
        });

        if (compilationUpdateDto.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            if (compilationUpdateDto.getEvents().size() != 0) {
                events = eventRepository.findEventsByIds(compilationUpdateDto.getEvents());
            }
            compilationToUpdate.setEvents(events);
        }

        compilationToUpdate.setPinned(compilationUpdateDto.getPinned() == null ?
                compilationToUpdate.getPinned() : compilationUpdateDto.getPinned());
        compilationToUpdate.setTitle(compilationUpdateDto.getTitle() == null ?
                compilationToUpdate.getTitle() : compilationUpdateDto.getTitle());

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilationToUpdate),
                compilationToUpdate.getEvents().stream().map(this::toShortEventDtoWithViews).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto compilationCreateDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationCreateDto);
        Set<Event> events = eventRepository.findEventsByIds(compilationCreateDto.getEvents());
        compilation.setEvents(events);
        List<EventShortDto> eventShortDtos = events
                .stream().map(this::toShortEventDtoWithViews)
                .collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation), eventShortDtos);
    }

    private EventShortDto toShortEventDtoWithViews(Event event) {
        return EventMapper.toEventShortDto(event, statsClient.getViews("/events/" + event.getId()).longValue());
    }
}