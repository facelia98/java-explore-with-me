package ru.practicum.services.interfaces;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.updates.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> get(Boolean pinned, Integer from, Integer size);

    CompilationDto getById(Long id);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationUpdateDto);

    CompilationDto saveCompilation(NewCompilationDto compilationCreateDto);

}