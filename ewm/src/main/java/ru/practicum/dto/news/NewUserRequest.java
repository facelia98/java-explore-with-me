package ru.practicum.dto.news;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @Length(min = 2, max = 250)
    @NotBlank
    private String name;
    @Length(min = 6, max = 254)
    @NotBlank
    @Email
    private String email;
}
