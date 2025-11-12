package ru.practicum.explorewithme.web.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.service.privateapi.PrivateEventRequestService;

import java.util.List;

/*
    Private: События
    Закрытый API для работы с событиями
    (Обработка всего по заявкам на евенты)
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestsController {

    private final PrivateEventRequestService privateEventRequestService;

    @GetMapping
    public List<ParticipationRequestDto> findUserEventRequests(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        log.info("Private find user event requests, params => userId: {}, eventId: {}", userId, eventId);

        return privateEventRequestService.listEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateStatusesForUserEventRequests(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest
    ) {
        log.info("Private update event statuses, params => userId: {}, eventId: {}, eventRequestStatusUpdateRequest: {}",
                userId, eventId, eventRequestStatusUpdateRequest);

        return privateEventRequestService.updateStatuses(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
