package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.ViewStatsClient;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.dto.updates.UpdateCommentDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.models.Comment;
import ru.practicum.repositories.CommentRepository;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.UserRepository;
import ru.practicum.services.interfaces.CommentService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ViewStatsClient viewStatsClient;

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {
        return commentRepository
                .findAllByEvent_Id(eventId, PageRequest.of(from, size)).stream()
                .map(comment -> CommentMapper.toCommentDto(comment))
                .sorted(Comparator.comparing(CommentShortDto::getCreated))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getById(Long commentId) {
        return getCommentDto(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Comment not found for id = {}", commentId);
            throw new NotFoundException("Comment not found for id = " + commentId);
        });
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentShortDto updateComment(Long commentId, UpdateCommentDto dto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Comment not found for id = {}", commentId);
            throw new NotFoundException("Comment not found for id = " + commentId);
        });
        comment.setComment(dto.getNewText());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto saveComment(Long userId, Long eventId, NewCommentDto dto) {
        Comment c = commentRepository.save(CommentMapper.toComment(dto,
                userRepository.getById(userId), eventRepository.getById(eventId)));
        return getCommentDto(c.getId());
    }

    @Override
    public CommentShortDto updateCommentUser(Long commentId, Long userId, UpdateCommentDto dto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.error("Comment not found for id = {}", commentId);
            throw new NotFoundException("Comment not found for id = " + commentId);
        });
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unallowed action: the user cannot delete or update the comment of other users");
            throw new ValidationException("Unallowed action: the user cannot delete or update the comment of other users");
        }
        comment.setComment(dto.getNewText());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentUser(Long commentId, Long userId) {
        if (!getCommentDto(commentId).getAuthor().getId().equals(userId)) {
            log.error("Unallowed action: the user cannot delete or update the comment of other users");
            throw new ValidationException("Unallowed action: the user cannot delete or update the comment of other users");
        }
        commentRepository.deleteById(commentId);
    }

    private CommentDto getCommentDto(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> {
            log.error("Comment not found for id = {}", id);
            throw new NotFoundException("Comment not found for id = " + id);
        });
        EventShortDto dto = EventMapper.toEventShortDto(comment.getEvent(), viewStatsClient.getViews("/events/" + comment.getEvent().getId()).longValue());
        return CommentMapper.toCommentDto(comment, dto);
    }
}
