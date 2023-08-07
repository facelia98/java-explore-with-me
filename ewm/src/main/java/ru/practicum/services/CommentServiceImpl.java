package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.ViewStatsClient;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentShortDto;
import ru.practicum.dto.news.NewCommentDto;
import ru.practicum.dto.updates.UpdateCommentDto;
import ru.practicum.enums.Status;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.models.Comment;
import ru.practicum.models.Event;
import ru.practicum.models.User;
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
                .map(comment -> CommentMapper.toCommentShortDto(comment))
                .sorted(Comparator.comparing(CommentShortDto::getCreated))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getById(Long commentId) {
        return CommentMapper.toCommentDto(getComment(commentId));
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        getComment(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentShortDto updateCommentAdmin(Long commentId, UpdateCommentDto dto) {
        Comment comment = getComment(commentId);
        comment.setComment(dto.getNewText());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentShortDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto saveComment(Long userId, Long eventId, NewCommentDto dto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event not found for id = {}", eventId);
            throw new NotFoundException("Event not found for id = " + eventId);
        });
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found for id = {}", userId);
            throw new NotFoundException("User not found for id = " + userId);
        });
        if (event.getEventState() != Status.PUBLISHED) {
            log.error("Couldn't save comment to unpublished event");
            throw new NotFoundException("Couldn't save comment to unpublished event");
        }
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(dto, user, event)));
    }

    @Override
    public CommentShortDto updateCommentUser(Long commentId, Long userId, UpdateCommentDto dto) {
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unallowed action: the user cannot delete or update the comment of other users");
            throw new ValidationException("Unallowed action: the user cannot delete or update the comment of other users");
        }
        comment.setComment(dto.getNewText());
        comment.setUpdated(LocalDateTime.now());
        return CommentMapper.toCommentShortDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentUser(Long commentId, Long userId) {
        if (!getComment(commentId).getAuthor().getId().equals(userId)) {
            log.error("Unallowed action: the user cannot delete or update the comment of other users");
            throw new ValidationException("Unallowed action: the user cannot delete or update the comment of other users");
        }
        commentRepository.deleteById(commentId);
    }

    private Comment getComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> {
            log.error("Comment not found for id = {}", id);
            throw new NotFoundException("Comment not found for id = " + id);
        });
    }
}
