package ru.practicum.explorewithme.web.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;
import ru.practicum.explorewithme.service.publicapi.PublicEventService;

import java.util.List;

/*
    Public: События
    Публичный API для работы с событиями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController {

    private final PublicEventService publicEventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @Valid @ModelAttribute PublicEventSearchCriteriaDto publicEventSearchCriteriaDto,
            HttpServletRequest request
    ) {
        log.info("Public list events, params => publicEventSearchCriteria: {}", publicEventSearchCriteriaDto);

        return publicEventService.search(publicEventSearchCriteriaDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(
            @PathVariable("eventId") Long eventId,
            HttpServletRequest request
    ) {
        log.info("Public event details, params => eventId: {}", eventId);

        return publicEventService.getById(eventId);
    }
}
