package ru.practicum.explorewithme.web.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.service.publicapi.PublicCompilationService;

import java.util.List;

/*
    Public: Подборки событий
    Публичный API для работы с подборками событий
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationsController {

    private final PublicCompilationService publicCompilationService;

    @GetMapping
    public List<CompilationDto> findAll(
            @RequestParam(name = "pinned", required = false) Boolean pinned,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        log.info("Public list compilations, pinned: {}, from: {}, size: {}", pinned, from, size);

        return publicCompilationService.findAll(pinned, from, size);
    }

    @GetMapping("/{compilationId}")
    public CompilationDto getById(@PathVariable("compilationId") Long compilationId) {
        log.info("Public get compilation by id: {}", compilationId);

        return publicCompilationService.getById(compilationId);
    }
}
