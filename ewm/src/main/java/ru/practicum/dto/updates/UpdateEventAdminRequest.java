package ru.practicum.dto.updates;


import lombok.*;
import ru.practicum.enums.EventState;
import ru.practicum.models.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    private String annotation;
    private Long categoryId;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private EventState eventStateAction;
    private String title;
}
