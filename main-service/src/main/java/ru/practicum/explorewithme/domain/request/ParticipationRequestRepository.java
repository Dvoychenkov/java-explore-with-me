package ru.practicum.explorewithme.domain.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.domain.event.Event;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventAndStatus(Event event, RequestStatus status);
}
