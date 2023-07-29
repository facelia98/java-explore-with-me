package ru.practicum.dto.news;

import lombok.*;
import ru.practicum.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
