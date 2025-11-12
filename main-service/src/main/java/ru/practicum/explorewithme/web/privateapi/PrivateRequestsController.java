package ru.practicum.explorewithme.web.privateapi;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.service.privateapi.PrivateRequestService;

import java.util.List;

/*
    Private: Запросы на участие
    Закрытый API для работы с запросами текущего пользователя на участие в событиях
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestsController {

    private final PrivateRequestService privateRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(
            @PathVariable("userId") Long userId,
            @RequestParam("eventId") @NotNull Long eventId
    ) {
        log.info("Private create request, params => userId: {}, eventId: {}", userId, eventId);

        return privateRequestService.create(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> findUserRequests(
            @PathVariable("userId") Long userId
    ) {
        log.info("Private list events request, params => userId: {}", userId);

        return privateRequestService.findUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId
    ) {
        log.info("Private cancel request, params => userId: {}, requestId: {}", userId, requestId);

        return privateRequestService.cancel(userId, requestId);
    }
}
