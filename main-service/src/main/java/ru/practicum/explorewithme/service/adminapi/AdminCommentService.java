package ru.practicum.explorewithme.service.adminapi;

import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.CommentDto;

import java.util.List;

public interface AdminCommentService {

    List<CommentDto> listByEvent(Long eventId, int from, int size);

    CommentDto setStatus(Long commentId, CommentStatus commentStatus);

    void delete(Long commentId);
}