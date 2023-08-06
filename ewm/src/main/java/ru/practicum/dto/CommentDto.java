package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    @JsonProperty("comment_text")
    private String commentText;
    private UserShortDto author;
    private EventShortDto event;
    private LocalDateTime created;
}
