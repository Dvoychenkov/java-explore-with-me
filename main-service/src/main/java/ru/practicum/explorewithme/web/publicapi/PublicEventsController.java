package ru.practicum.explorewithme.web.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.service.publicapi.PublicEventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.NewHitDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.util.DateTimeUtils.ISO_DATE_TIME_FORMATTER;

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

    private final StatsClient statsClient;
    private final PublicEventService publicEventService;

    @Value("${stats-service.app-name}")
    private String appName;


    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "categories", required = false) List<Long> categories,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size,
            HttpServletRequest request
    ) {
        log.info("Public list events");

        // TODO улучшить
        statsClient.saveHit(NewHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(ISO_DATE_TIME_FORMATTER))
                .build());

        return publicEventService.search(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size
        );
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(
            @PathVariable("id") Long eventId,
            HttpServletRequest request
    ) {
        log.info("Public get event details by id: {}", eventId);

        // TODO улучшить
        statsClient.saveHit(NewHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(ISO_DATE_TIME_FORMATTER))
                .build());

        return publicEventService.getById(eventId);
    }
}
