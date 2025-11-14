package ru.practicum.explorewithme.web.publicapi;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.service.publicapi.PublicCommentService;

import java.util.List;

/*
    Public: Комментарии
    Публичный API для работы с комментариями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class PublicCommentsController {

    private final PublicCommentService publicCommentService;

    @GetMapping
    public List<CommentDto> list(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size
    ) {
        log.info("Public list comments, params => eventId: {}, from: {}, size: {}", eventId, from, size);

        return publicCommentService.listByEvent(eventId, from, size);
    }
}