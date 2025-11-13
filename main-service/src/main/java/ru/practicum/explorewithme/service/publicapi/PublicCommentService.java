package ru.practicum.explorewithme.service.publicapi;

import ru.practicum.explorewithme.dto.comment.CommentDto;

import java.util.List;

public interface PublicCommentService {

    List<CommentDto> listByEvent(Long eventId, int from, int size);
}