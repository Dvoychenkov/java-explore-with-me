package ru.practicum.explorewithme.domain.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query(
            "SELECT pr.event.id as eventId, COUNT(pr) as requestCount " +
                    "FROM ParticipationRequest pr " +
                    "WHERE pr.event.id IN :eventIds " +
                        "AND pr.status = :status " +
                    "GROUP BY pr.event.id"
    )
    List<EventRequestCount> findRequestsCountByEventIdsAndStatus(List<Long> eventIds, RequestStatus status);
}
