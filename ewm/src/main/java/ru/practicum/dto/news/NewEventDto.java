package ru.practicum.dto.news;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.models.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    private Location location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Long participantLimit;
    private Boolean requestModeration;
    private Boolean paid;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    private Long views = 0L;
}
