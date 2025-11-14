package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.AdminUpdateCommentStatusDto;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.service.adminapi.AdminCommentService;

import java.util.List;

/*
    Admin: Комментарии
    API для работы с комментариями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentsController {

    private final AdminCommentService adminCommentService;

    @GetMapping("/events/{eventId}")
    public List<CommentDto> list(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size
    ) {
        log.info("Admin list comments, params => eventId: {}, from: {}, size: {}", eventId, from, size);

        return adminCommentService.listByEvent(eventId, from, size);
    }

    @PatchMapping("/{commentId}/status")
    public CommentDto setStatus(
            @PathVariable Long commentId,
            @Valid @RequestBody AdminUpdateCommentStatusDto adminUpdateCommentStatusDto
    ) {
        log.info("Admin set comment status, params => id: {}, adminUpdateCommentStatusDto: {}",
                commentId, adminUpdateCommentStatusDto);

        return adminCommentService.setStatus(commentId, adminUpdateCommentStatusDto.getStatus());
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        log.info("Admin delete comment, params => id: {}", commentId);

        adminCommentService.delete(commentId);
    }
}