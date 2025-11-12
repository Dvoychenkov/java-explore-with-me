package ru.practicum.explorewithme.service.publicapi;

import ru.practicum.explorewithme.dto.compilation.CompilationDto;

import java.util.List;

public interface PublicCompilationService {

    List<CompilationDto> findAll(Boolean pinned, int from, int size);

    CompilationDto getById(Long id);
}
