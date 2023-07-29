package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.enums.Status;
import ru.practicum.models.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestsRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> getParticipationRequestByEventAndRequester(Long eventId, Long userId);

    Long countParticipationByEventIdAndStatus(Long id, Status status);

    List<ParticipationRequest> findAllByRequesterId(Long requestorId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requestorId);

    List<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requestorId);
}
