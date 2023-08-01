package ru.practicum.dto.updates;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned;
    @Length(max = 50)
    private String title;
}