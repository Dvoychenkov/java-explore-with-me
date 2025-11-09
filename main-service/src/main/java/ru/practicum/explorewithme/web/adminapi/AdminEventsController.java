package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.adminapi.AdminEventService;
import ru.practicum.explorewithme.util.DateTimeUtils;

import java.util.List;

/*
    Admin: События
    API для работы с событиями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {

    private final AdminEventService adminEventService;

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(name = "users", required = false) List<Long> users,
            @RequestParam(name = "states", required = false) List<String> states,
            @RequestParam(name = "categories", required = false) List<Long> categories,
            @RequestParam(name = "rangeStart", required = false)
            @DateTimeFormat(pattern = DateTimeUtils.ISO_DATE_TIME_FORMAT) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false)
            @DateTimeFormat(pattern = DateTimeUtils.ISO_DATE_TIME_FORMAT) String rangeEnd,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size
    ) {
        log.info("Admin list events users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        return adminEventService.search(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest
    ) {
        log.info("Admin update event id: {}, event: {}", eventId, updateEventAdminRequest);

        return adminEventService.update(eventId, updateEventAdminRequest);
    }
}
