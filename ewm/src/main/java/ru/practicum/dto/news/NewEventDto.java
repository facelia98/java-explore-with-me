package ru.practicum.dto.news;

import lombok.*;
import ru.practicum.models.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    private String annotation;
    private Long categoryId;
    private String description;
    private Location location;
    private LocalDateTime eventDate;
    private Long participantLimit;
    private Boolean requestModeration;
    private Boolean paid;
    private String title;
    private Long views;
}
