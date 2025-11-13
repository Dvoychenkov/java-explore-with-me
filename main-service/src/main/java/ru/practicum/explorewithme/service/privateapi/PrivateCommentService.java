package ru.practicum.explorewithme.service.privateapi;

import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;

public interface PrivateCommentService {

    CommentDto add(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto update(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    void delete(Long userId, Long commentId);
}