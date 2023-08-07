package ru.practicum.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.models.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c where c.event.id = :id")
    List<Comment> findAllByEvent_Id(Long id, PageRequest request);

    @Query("select c from Comment c where c.event.id = :id")
    List<Comment> findAllByEvent_Id(Long id);
}
