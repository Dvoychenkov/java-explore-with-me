package ru.practicum.explorewithme.service.adminapi;

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
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> listByEvent(Long eventId, int from, int size) {
        Sort createdOnDescSort = Sort.by("createdOn").descending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, createdOnDescSort);
        Page<Comment> commentPage = commentRepository.findAllByEventId(eventId, offsetLimit);

        List<Comment> comments = commentPage.getContent();
        return commentMapper.toDtoList(comments);
    }

    @Override
    @Transactional
    public CommentDto setStatus(Long commentId, CommentStatus commentStatus) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        comment.setStatus(commentStatus);
        comment.setUpdatedOn(LocalDateTime.now());

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void delete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Comment not found: " + commentId);
        }

        commentRepository.deleteById(commentId);
    }
}