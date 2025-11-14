package ru.practicum.explorewithme.service.privateapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.comment.CommentRepository;
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;
import ru.practicum.explorewithme.mapper.CommentMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrivateCommentServiceImpl implements PrivateCommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto add(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new IllegalStateException("Comments allowed only for published events");
        }

        Comment comment = commentMapper.toEntity(newCommentDto, author, event);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PUBLISHED);

        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only author can edit comment");
        }

        commentMapper.updateEntity(updateCommentDto, comment);
        comment.setUpdatedOn(LocalDateTime.now());

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only author can delete comment");
        }

        commentRepository.delete(comment);
    }
}