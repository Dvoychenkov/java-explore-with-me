package ru.practicum.explorewithme.service.publicapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.comment.Comment;
import ru.practicum.explorewithme.domain.comment.CommentRepository;
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCommentServiceImpl implements PublicCommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> listByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new EntityNotFoundException("Event not published: " + eventId);
        }

        Sort createdOnDescSort = Sort.by("createdOn").descending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, createdOnDescSort);
        Page<Comment> commentPage = commentRepository.findPublishedByEvent(eventId, CommentStatus.PUBLISHED, offsetLimit);

        List<Comment> comments = commentPage.getContent();
        return commentMapper.toDtoList(comments);
    }
}