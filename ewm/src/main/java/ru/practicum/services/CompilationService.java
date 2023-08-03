package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.models.Compilation;
import ru.practicum.models.Event;
import ru.practicum.repositories.CompilationRepository;
import ru.practicum.repositories.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;


    @Transactional(readOnly = true)
    public List<CompilationDto> get(Integer from, Integer size) {
        log.info("GET Compilations request received");
        return compilationRepository.findAll(PageRequest.of(from, size)).get()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public CompilationDto getById(Long id) {
        log.info("GET Compilation request received with id = {}", id);
        if (!compilationRepository.existsById(id)) {
            log.error("Compilation not found for id = {}", id);
            throw new NotFoundException("Compilation not found for id = " + id);
        }
        return CompilationMapper.toCompilationDto(compilationRepository.getById(id));
    }


    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("DELETE Compilation request received with id = {}", compId);
        compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Incorrect id"));
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationUpdateDto) {
        log.info("PATCH Compilation request received with id = {}", compId);
        Compilation compilationToUpdate = compilationRepository.getById(compId);

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

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilationToUpdate));
    }


    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto compilationCreateDto) {
        log.info("POST Compilation request received");
        Compilation compilation = CompilationMapper.toCompilation(compilationCreateDto);
        Set<Event> events = eventRepository.findEventsByIds(compilationCreateDto.getEvents());
        compilation.setEvents(events);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}