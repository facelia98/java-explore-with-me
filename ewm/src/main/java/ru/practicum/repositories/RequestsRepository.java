package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.Status;
import ru.practicum.models.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestsRepository extends JpaRepository<ParticipationRequest, Long> {
    @Query("select pr from ParticipationRequest pr where pr.event.id = :eventId")
    List<ParticipationRequest> findAllByEvent_Id(Long eventId);

    @Query("select pr from ParticipationRequest pr where pr.status = :status and pr.event.id = :event")
    List<ParticipationRequest> countParticipationRequests(Long event, Status status);

    List<ParticipationRequest> findAllByRequesterId(Long requestorId);

    Optional<ParticipationRequest> findByIdAndEvent_Id(Long id, Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requestorId);

    List<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requestorId);
}