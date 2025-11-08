package ru.practicum.explorewithme.domain.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.domain.event.Event;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventAndStatus(Event event, RequestStatus requestStatus);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);
}
