package ru.practicum.mappers;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.models.Comment;
import ru.practicum.models.Event;
import ru.practicum.models.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, EventShortDto event) {
        return CommentDto.builder()
                .id(comment.getId())
                .comment_text(comment.getComment())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated())
                .event(event)
                .build();
    }

    public static CommentShortDto toCommentDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .comment_text(comment.getComment())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(NewCommentDto dto, User author, Event event) {
        return Comment.builder()
                .comment(dto.getComment_text())
                .author(author)
                .created(dto.getCreated() == null ? LocalDateTime.now() : dto.getCreated())
                .event(event)
                .build();
    }
}
