package ru.practicum.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentShortDto {
    private Long id;
    private String comment_text;
    private UserShortDto author;
    private LocalDateTime created;
}
