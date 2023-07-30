package ru.practicum.dto.news;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class NewCategoryDto {
    @Length(max = 50)
    @NotBlank
    private String name;
}
