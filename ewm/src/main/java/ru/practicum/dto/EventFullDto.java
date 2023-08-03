package ru.practicum.dto;

import lombok.*;
import ru.practicum.enums.Status;
import ru.practicum.models.Location;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private Status state;
    private String title;
    private Long views;
    private Long confirmedRequests;
}
