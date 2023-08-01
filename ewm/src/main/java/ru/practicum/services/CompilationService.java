package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.models.Compilation;
import ru.practicum.models.Event;
import ru.practicum.repositories.CompilationRepository;
import ru.practicum.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public List<CompilationDto> get(Integer from, Integer size) {
        return compilationRepository.findAll(PageRequest.of(from, size)).get()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getById(Long id) {
        if (!compilationRepository.existsById(id)) {
            log.error("Compilation not found for id = {}", id);
            throw new NotFoundException("Compilation not found for id = " + id);
        }
        return CompilationMapper.toCompilationDto(compilationRepository.getById(id));
    }

    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Incorrect id"));
        compilationRepository.deleteById(compId);
    }

    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationUpdateDto) {
        Compilation compilationToUpdate = compilationRepository.getById(compId);

        if (compilationUpdateDto.getEvents() != null) {
            List<Event> events = new ArrayList<>();
            if (compilationUpdateDto.getEvents().size() != 0) {
                events = eventRepository.findEventsByIds(compilationUpdateDto.getEvents());
            }
            compilationToUpdate.setEvents(events);
        }
        compilationToUpdate.setPinned(compilationUpdateDto.getPinned() == null ?
                compilationToUpdate.getPinned() : compilationUpdateDto.getPinned());
        compilationToUpdate.setTitle(compilationUpdateDto.getTitle() == null ?
                compilationToUpdate.getTitle() : compilationUpdateDto.getTitle());

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilationToUpdate));
    }

    public CompilationDto saveCompilation(NewCompilationDto compilationCreateDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationCreateDto);
        List<Event> events = eventRepository.findEventsByIds(compilationCreateDto.getEvents());
        compilation.setEvents(events);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}