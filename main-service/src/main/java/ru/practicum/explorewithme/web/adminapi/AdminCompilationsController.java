package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.service.adminapi.AdminCompilationService;

/*
    Admin: Подборки событий
    API для работы с подборками событий
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationsController {

    private final AdminCompilationService adminCompilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(
            @Valid @RequestBody NewCompilationDto newCompilationDto
    ) {
        log.info("Admin create, params => newCompilationDto : {}", newCompilationDto);

        return adminCompilationService.create(newCompilationDto);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto update(
            @PathVariable Long compilationId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest
    ) {
        log.info("Admin update compilation, params => compilationId: {}, updateCompilationRequest: {}",
                compilationId, updateCompilationRequest);

        return adminCompilationService.update(compilationId, updateCompilationRequest);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long compilationId
    ) {
        log.info("Admin delete, params => compilationId: {}", compilationId);

        adminCompilationService.delete(compilationId);
    }
}
