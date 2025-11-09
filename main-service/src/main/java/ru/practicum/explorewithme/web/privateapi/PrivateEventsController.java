package ru.practicum.explorewithme.web.privateapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.service.privateapi.PrivateEventService;

import java.util.List;

/*
    Private: События
    Закрытый API для работы с событиями
    (Обработка всего по самим евентам)
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventsController {

    private final PrivateEventService privateEventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        log.info("Private create event by user id: {}, event: {}", userId, newEventDto);

        return privateEventService.create(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> findUserEvents(
            @PathVariable("userId") Long userId,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size
    ) {
        log.info("Private list user events info by user id: {}, from: {}, size: {}", userId, from, size);

        return privateEventService.findUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        log.info("Private user event info by user id: {}, event id: {}", userId, eventId);

        return privateEventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest
    ) {
        log.info("Private update event by event id: {}, user id: {}, event: {}", eventId, userId, updateEventUserRequest);

        return privateEventService.update(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/cancel")
    public EventFullDto cancel(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        log.info("Private cancel event by event id: {}, user id: {}", eventId, userId);

        return privateEventService.cancel(userId, eventId);
    }
}
