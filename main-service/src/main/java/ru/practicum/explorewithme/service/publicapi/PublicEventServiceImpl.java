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
import ru.practicum.explorewithme.domain.request.EventRequestCount;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.EventSort;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.stats.StatsViewsService;
import ru.practicum.explorewithme.util.DateTimeUtils;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.util.AppConstants.EVENTS_URL_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventMapper eventMapper;
    private final StatsViewsService statsViewsService;

    @Value("${stats-service.collect-stats-by-years-cnt}")
    private int collectStatsByYearsCnt;

    @Override
    public List<EventShortDto> search(PublicEventSearchCriteriaDto publicEventSearchCriteriaDto) {
        DateTimeUtils.validateDateRange(
                publicEventSearchCriteriaDto.getRangeStart(), publicEventSearchCriteriaDto.getRangeEnd());

        List<Event> events = doSearch(publicEventSearchCriteriaDto);

        return doBusinessLogicAndConvertToDtos(events, publicEventSearchCriteriaDto);
    }

    @Override
    public EventFullDto getById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        // Евент должен быть опубликован
        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException("Event not found: " + eventId);
        }
        EventFullDto eventFullDto = eventMapper.toFullDto(event);

        long confirmed = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        eventFullDto.setConfirmedRequests(confirmed);

        Map<String, Long> views = statsViewsService.fetchViews(
                event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn(),
                LocalDateTime.now(),
                List.of(EVENTS_URL_PREFIX + event.getId()),
                true
        );

        eventFullDto.setViews(views.getOrDefault(EVENTS_URL_PREFIX + event.getId(), 0L));

        return eventFullDto;
    }

    private List<Event> doSearch(PublicEventSearchCriteriaDto publicEventSearchCriteriaDto) {
        // Билдим запрос с критериями по поиску
        Sort eventsSort;
        if (EventSort.EVENT_DATE.equals(publicEventSearchCriteriaDto.getSort())) {
            eventsSort = Sort.by(Sort.Direction.ASC, "eventDate");
        } else {
            eventsSort = Sort.unsorted(); // Для другой сортировки сначала нужно получить статистику по эндпоинтам
        }
        Pageable pageable = QueryUtils.offsetLimit(
                publicEventSearchCriteriaDto.getFrom(), publicEventSearchCriteriaDto.getSize(), eventsSort);

        Specification<Event> eventSpecification = EventSpecifications.publicSearch(publicEventSearchCriteriaDto);

        if (Boolean.TRUE.equals(publicEventSearchCriteriaDto.getOnlyAvailable())) {
            eventSpecification = eventSpecification.and(EventSpecifications.onlyAvailable());
        }

        // Ищем
        return eventRepository.findAll(eventSpecification, pageable).getContent();
    }

    private List<EventShortDto> doBusinessLogicAndConvertToDtos(
            List<Event> events, PublicEventSearchCriteriaDto publicEventSearchCriteriaDto) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Собираем id для batch-запросов
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        List<String> uris = eventIds.stream()
                .map(id -> EVENTS_URL_PREFIX + id)
                .collect(Collectors.toList());

        // Получаем подтверждённые запросы
        List<EventRequestCount> confirmedRequestsMapList = participationRequestRepository
                .findRequestsCountByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequestsMap = confirmedRequestsMapList.stream()
                .collect(Collectors.toMap(EventRequestCount::getEventId,EventRequestCount::getRequestCount));

        LocalDateTime periodStart = (publicEventSearchCriteriaDto.getRangeStart() != null) ?
                publicEventSearchCriteriaDto.getRangeStart() :
                LocalDateTime.now().minusYears(collectStatsByYearsCnt);
        LocalDateTime periodEnd = (publicEventSearchCriteriaDto.getRangeEnd() != null) ?
                publicEventSearchCriteriaDto.getRangeEnd() :
                LocalDateTime.now();

        // Получаем статистику просмотров
        Map<String, Long> viewsMap = statsViewsService.fetchViews(periodStart, periodEnd, uris, false);

        List<EventShortDto> result = events.stream()
                .map(event -> convertToEventShortDto(event, confirmedRequestsMap, viewsMap))
                .collect(Collectors.toList());

        // Применяем сортировку по просмотрам при необходимости
        if (EventSort.VIEWS.equals(publicEventSearchCriteriaDto.getSort())) {
            result.sort(Comparator.comparingLong(
                    dto -> -1L * Optional.ofNullable(dto.getViews()).orElse(0L)
            ));
        }

        return result;
    }

    private EventShortDto convertToEventShortDto(
            Event event, Map<Long, Long> confirmedRequestsMap, Map<String, Long> viewsMap) {
        EventShortDto eventShortDto = eventMapper.toShortDto(event);

        // Устанавливаем подтверждённые запросы
        Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
        eventShortDto.setConfirmedRequests(confirmedRequests);

        // Устанавливаем просмотры
        String eventUri = EVENTS_URL_PREFIX + event.getId();
        Long views = viewsMap.getOrDefault(eventUri, 0L);
        eventShortDto.setViews(views);

        return eventShortDto;
    }
}
