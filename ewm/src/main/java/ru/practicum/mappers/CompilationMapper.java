package ru.practicum.mappers;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.models.Compilation;

import java.util.stream.Collectors;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream().map(EventMapper::toEventShortDto).collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle()).build();
    }


    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle()).build();
    }
}
