package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.AdminEventSearchCriteriaDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.adminapi.AdminEventService;

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
            @Valid @ModelAttribute AdminEventSearchCriteriaDto adminEventSearchCriteriaDto
    ) {
        log.info("Admin list events; params => adminEventSearchCriteriaDto: {}", adminEventSearchCriteriaDto);

        return adminEventService.search(adminEventSearchCriteriaDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest
    ) {
        log.info("Admin update event, params => eventId: {}; updateEventAdminRequest: {}",
                eventId, updateEventAdminRequest);

        return adminEventService.update(eventId, updateEventAdminRequest);
    }
}
