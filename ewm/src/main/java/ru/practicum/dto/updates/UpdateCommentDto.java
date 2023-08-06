package ru.practicum.dto.updates;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {
    @NotBlank
    private String newText;
}
