package ru.practicum.explorewithme.service.publicapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.compilation.Compilation;
import ru.practicum.explorewithme.domain.compilation.CompilationRepository;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.stats.StatsViewsService;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final StatsViewsService statsViewsService;

    @Value("${stats-service.collect-stats-by-years-cnt}")
    private int collectStatsByYearsCnt;

    // TODO вынести в константы
    private static final String EVENTS_URL_PREFIX = "/events/";

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Sort idAscSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, idAscSort);
        Page<Compilation> compilationPage = (pinned == null) ?
                compilationRepository.findAll(offsetLimit) :
                compilationRepository.findAllByPinned(pinned, offsetLimit);

        List<CompilationDto> compilationDtos = compilationPage
                .map(compilationMapper::toDto)
                .getContent();
        injectViews(compilationDtos);

        return compilationDtos;
    }

    @Override
    public CompilationDto getById(Long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compilation not found by id: " + id));

        CompilationDto compilationDto = compilationMapper.toDto(compilation);
        injectViews(List.of(compilationDto));

        return compilationDto;
    }

    // Собираем все URI /events/{id} из событий всех подборок
    private void injectViews(List<CompilationDto> compilationDtos) {
        List<String> uris = new ArrayList<>();
        for (CompilationDto compilationDto : compilationDtos) {
            for (EventShortDto eventShortDto : compilationDto.getEvents()) {
                uris.add(EVENTS_URL_PREFIX + eventShortDto.getId());
            }
        }

        if (uris.isEmpty()) {
            return;
        }

        LocalDateTime periodStart = LocalDateTime.now().minusYears(collectStatsByYearsCnt);
        LocalDateTime periodEnd = LocalDateTime.now();

        Map<String, Long> statsViews = statsViewsService.fetchViews(periodStart, periodEnd, uris, false);

        for (CompilationDto compilationDto : compilationDtos) {
            for (EventShortDto eventShortDto : compilationDto.getEvents()) {
                Long viewsCnt = statsViews.getOrDefault(EVENTS_URL_PREFIX + eventShortDto.getId(), 0L);
                eventShortDto.setViews(viewsCnt);
            }
        }
    }
}
