package ru.practicum.explorewithme.service.publicapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventSpecifications;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.stats.StatsViewsService;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsViewsService statsViewsService;

    @Value("${stats-service.collect-stats-by-years-cnt}")
    private int collectStatsByYearsCnt;

    private static final DateTimeFormatter ISO_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // TODO вынести в константы
    private static final String EVENTS_URL_PREFIX = "/events/";

    @Override
    public List<EventShortDto> search(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                                      Boolean onlyAvailable, String sort, Integer from, Integer size) {

        LocalDateTime start = parseOrNull(rangeStart);
        LocalDateTime end = parseOrNull(rangeEnd);
        // TODO вынести проверку дат в утилиту?
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }

        Sort jpaSort;
        // TODO вынести строки сортировки в enum
        if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            jpaSort = Sort.by(Sort.Direction.ASC, "eventDate");
        } else {
            // при сортировке по VIEWS — отдаём unsorted, а сортировку сделаем после подстановки просмотров
            jpaSort = Sort.unsorted();
        }
        Pageable pageable = QueryUtils.offsetLimit(from, size, jpaSort);

        Specification<Event> eventSpecification = EventSpecifications.publicSearch(text, categories, paid, start, end);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            eventSpecification = eventSpecification.and(EventSpecifications.onlyAvailable());
        }

        List<Event> page = eventRepository.findAll(eventSpecification, pageable).getContent();

        // TODO Добавить в маппер обработку листов
        List<EventShortDto> resultEventShortDtos = new ArrayList<>(page.size());
        for (Event event : page) {
            resultEventShortDtos.add(eventMapper.toShortDto(event));
        }

        if (resultEventShortDtos.isEmpty()) {
            return resultEventShortDtos;
        }

        // Получаем статистику и применяем сортировку по VIEWS при необходимости
        List<String> uris = new ArrayList<>(resultEventShortDtos.size());
        for (EventShortDto eventShortDto : resultEventShortDtos) {
            uris.add(EVENTS_URL_PREFIX + eventShortDto.getId());
        }

        LocalDateTime periodStart = (start != null) ?
                start :
                LocalDateTime.now().minusYears(collectStatsByYearsCnt);
        LocalDateTime periodEnd = end != null ? end : LocalDateTime.now();

        Map<String, Long> views = statsViewsService.fetchViews(periodStart, periodEnd, uris, false);

        for (EventShortDto eventShortDto : resultEventShortDtos) {
            Long viewsCnt = views.getOrDefault(EVENTS_URL_PREFIX + eventShortDto.getId(), 0L);
            eventShortDto.setViews(viewsCnt);
        }

        // TODO вынести строки сортировки в enum
        if ("VIEWS".equalsIgnoreCase(sort)) {
            resultEventShortDtos.sort(Comparator.comparingLong(
                    eventShortDto -> -1L * Optional.ofNullable(eventShortDto.getViews()).orElse(0L)
            ));
        }

        return resultEventShortDtos;
    }

    @Override
    public EventFullDto getById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        // TODO обработка EntityNotFoundException в @RestControllerAdvice ?
        // Cобытие должно быть опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException("Event not found: " + eventId);
        }
        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        Map<String, Long> views = statsViewsService.fetchViews(
                event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn(),
                LocalDateTime.now(),
                List.of(EVENTS_URL_PREFIX + event.getId()),
                false
        );

        eventFullDto.setViews(views.getOrDefault(EVENTS_URL_PREFIX + event.getId(), 0L));

        return eventFullDto;
    }

    // TODO вынести в утилиту
    private LocalDateTime parseOrNull(String possibleDateTime) {
        if (possibleDateTime == null || possibleDateTime.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(possibleDateTime, ISO_DATE_TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            // TODO брать формат из шаблона?
            throw new IllegalArgumentException("start/end must match 'yyyy-MM-dd HH:mm:ss'");
        }
    }
}
