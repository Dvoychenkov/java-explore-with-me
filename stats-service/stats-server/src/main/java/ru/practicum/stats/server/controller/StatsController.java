package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;

import java.util.List;

import static ru.practicum.stats.server.util.DateTimeUtils.ISO_DATE_TIME_FORMAT;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto saveHit(@Valid @RequestBody NewHitDto newHitDto) {
        log.info("Save hit request: {}", newHitDto);
        return service.saveHit(newHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = ISO_DATE_TIME_FORMAT) String start,
            @RequestParam @DateTimeFormat(pattern = ISO_DATE_TIME_FORMAT) String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        log.info("Get stats request: start {}; end {}; uris {}; unique {}", start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }
}
