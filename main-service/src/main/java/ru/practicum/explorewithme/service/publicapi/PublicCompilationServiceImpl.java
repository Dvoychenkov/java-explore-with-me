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
import ru.practicum.explorewithme.domain.request.EventRequestCount;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.stats.StatsViewsService;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.util.AppConstants.EVENTS_URL_PREFIX;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCompilationServiceImpl implements PublicCompilationService {

    private final CompilationRepository compilationRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CompilationMapper compilationMapper;
    private final StatsViewsService statsViewsService;

    @Value("${stats-service.collect-stats-by-years-cnt}")
    private int collectStatsByYearsCnt;

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Sort idAscSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, idAscSort);
        Page<Compilation> compilationPage = (pinned == null) ?
                compilationRepository.findAll(offsetLimit) :
                compilationRepository.findAllByPinned(pinned, offsetLimit);
        List<Compilation> compilations = compilationPage.getContent();

        List<CompilationDto> compilationDtos = compilationMapper.toDtoList(compilations);

        injectEventStats(compilationDtos);
        return compilationDtos;
    }

    @Override
    public CompilationDto getById(Long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compilation not found by id: " + id));

        CompilationDto compilationDto = compilationMapper.toDto(compilation);

        injectEventStats(List.of(compilationDto));
        return compilationDto;
    }

    private void injectEventStats(List<CompilationDto> compilationDtos) {
        // Собираем все уникальные евенты
        Set<EventShortDto> allEvents = compilationDtos.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toSet());

        if (allEvents.isEmpty()) {
            return;
        }

        // Собираем id для batch-запросов
        List<Long> eventIds = allEvents.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());
        List<String> uris = eventIds.stream()
                .map(id -> EVENTS_URL_PREFIX + id)
                .collect(Collectors.toList());

        // Получаем подтверждённые запросы
        List<EventRequestCount> confirmedRequestsMapList = participationRequestRepository
                .findRequestsCountByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequestsMap = confirmedRequestsMapList.stream()
                .collect(Collectors.toMap(EventRequestCount::getEventId, EventRequestCount::getRequestCount));

        // Получаем статистику просмотров
        Map<String, Long> viewsMap = statsViewsService.fetchViews(
                LocalDateTime.now().minusYears(collectStatsByYearsCnt),
                LocalDateTime.now(),
                uris,
                false
        );

        // Обновляем все события
        Map<Long, EventShortDto> eventMap = allEvents.stream()
                .collect(
                        Collectors.toMap(EventShortDto::getId, Function.identity())
                );

        // Обогащаем евенты
        eventMap.forEach((eventId, eventDto) -> {
            eventDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(eventId, 0L));
            eventDto.setViews(viewsMap.getOrDefault(EVENTS_URL_PREFIX + eventId, 0L));
        });
    }
}
