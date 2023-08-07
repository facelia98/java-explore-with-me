package ru.practicum.services.interfaces;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.dto.updates.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    List<CommentShortDto> getCommentsByEvent(Long eventId, Integer from, Integer size);

    CommentDto getById(Long commentId);

    void deleteCommentAdmin(Long commentId);

    CommentShortDto updateCommentAdmin(Long commentId, UpdateCommentDto dto);

    CommentDto saveComment(Long userId, Long eventId, NewCommentDto dto);

    CommentShortDto updateCommentUser(Long commentId, Long userId, UpdateCommentDto commentDto);

    void deleteCommentUser(Long commentId, Long userId);
}
