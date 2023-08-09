package ru.practicum.mappers;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.models.Comment;
import ru.practicum.models.Event;
import ru.practicum.models.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .commentText(comment.getComment())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .event(comment.getEvent().getId())
                .build();
    }

    public static CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .commentText(comment.getComment())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .updated(comment.getUpdated())
                .created(comment.getCreated())
                .build();
    }

    public static Comment toComment(NewCommentDto dto, User author, Event event) {
        return Comment.builder()
                .comment(dto.getCommentText())
                .author(author)
                .created(dto.getCreated() == null ? LocalDateTime.now() : dto.getCreated())
                .event(event)
                .build();
    }
}
