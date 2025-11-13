package ru.practicum.explorewithme.web.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;
import ru.practicum.explorewithme.service.privateapi.PrivateCommentService;

/*
    Private: Комментарии
    Закрытый API для работы с комментариями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
public class PrivateCommentsController {

    private final PrivateCommentService privateCommentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto
    ) {
        log.info("Private add comment, params => userId: {}, eventId: {}, newCommentDto: {}",
                userId, eventId, newCommentDto);

        return privateCommentService.add(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto
    ) {
        log.info("Private update comment, params => userId: {}, eventId: {}, commentId: {}, updateCommentDto: {}",
                userId, eventId, commentId, updateCommentDto);

        return privateCommentService.update(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId
    ) {
        log.info("Private delete comment, params => userId: {}, eventId: {}, commentId: {}", userId, eventId, commentId);

        privateCommentService.delete(userId, commentId);
    }
}