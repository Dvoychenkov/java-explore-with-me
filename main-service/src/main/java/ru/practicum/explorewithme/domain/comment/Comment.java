package ru.practicum.explorewithme.domain.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(length = 2000, nullable = false)
    private String text;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CommentStatus status;
}