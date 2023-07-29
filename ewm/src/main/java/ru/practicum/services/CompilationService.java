package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CategoryMapper;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.models.Compilation;
import ru.practicum.models.Event;
import ru.practicum.repositories.CompilationRepository;
import ru.practicum.repositories.EventRepository;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
                .map(compilation -> CompilationMapper.toCompilationDto(compilation))
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

        if (compilationToUpdate.getEvents() != null) {
            List<Event> events = new ArrayList<>();
            if (compilationUpdateDto.getEvents().size() != 0) {
                events = eventRepository.findEventsByIds(compilationUpdateDto.getEvents());
            }
            compilationToUpdate.setEvents(events);
        }
        compilationToUpdate.setPinned(compilationUpdateDto.getPinned());
        compilationToUpdate.setTitle(compilationUpdateDto.getTitle());

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilationToUpdate));
    }

    public CompilationDto saveCompilation(NewCompilationDto compilationCreateDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationCreateDto);
        List<Event> events = eventRepository.findEventsByIds(compilation.getEvents()
                .stream()
                .map(event -> event.getId())
                .collect(Collectors.toList()));
        compilation.setEvents(events);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }
}
