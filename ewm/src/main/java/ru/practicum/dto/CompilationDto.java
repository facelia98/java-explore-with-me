package ru.practicum.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CompilationDto {
    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    @NotBlank
    @Length(max = 50)
    private String title;
}
