package ru.practicum.explorewithme.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(
            "SELECT c " +
                    "FROM Comment c " +
                    "WHERE c.event.id = :eventId " +
                    "AND c.status = :commentStatus "
    )
    Page<Comment> findPublishedByEvent(Long eventId, CommentStatus commentStatus, Pageable pageable);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);
}