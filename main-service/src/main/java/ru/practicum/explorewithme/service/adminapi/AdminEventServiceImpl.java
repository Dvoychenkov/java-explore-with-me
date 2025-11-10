package ru.practicum.explorewithme.service.adminapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.explorewithme.dto.event.AdminEventSearchCriteriaDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventStateAction;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> search(AdminEventSearchCriteriaDto adminEventSearchCriteriaDto) {
        Specification<Event> eventSpecification = EventSpecifications.adminSearch(adminEventSearchCriteriaDto);

        Sort idDescSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(
                adminEventSearchCriteriaDto.getFrom(), adminEventSearchCriteriaDto.getSize(), idDescSort);

        List<Event> events = eventRepository.findAll(eventSpecification, offsetLimit).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        List<EventRequestCount> confirmedRequestsMapList = participationRequestRepository.findRequestsCountByEventIdsAndStatus(
                eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> confirmedRequestsMap = confirmedRequestsMapList.stream()
                .collect(Collectors.toMap(EventRequestCount::getEventId,EventRequestCount::getRequestCount));

        return events.stream()
                .map(event -> {
                    EventFullDto eventFullDto = eventMapper.toFullDto(event);

                    long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                    eventFullDto.setConfirmedRequests(confirmedRequests);

                    return eventFullDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        LocalDateTime eventDate = updateEventAdminRequest.getEventDate();
        if (eventDate != null) {
            // Дата начала изменяемого события должна быть не ранее чем за час от даты публикации
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("eventDate must be at least 1 hour in the future");
            }
        }

        EventStateAction stateAction = updateEventAdminRequest.getStateAction();
        if (stateAction != null) {
            if (EventStateAction.PUBLISH_EVENT.equals(stateAction)) {
                if (event.getState() != EventState.PENDING) {
                    throw new IllegalStateException(String.format("Only events in state '%s' can be published",
                            EventState.PENDING));
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (EventStateAction.REJECT_EVENT.equals(stateAction)) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new IllegalStateException(String.format("Events in state '%s' can't be rejected",
                            EventState.PUBLISHED));
                }
                event.setState(EventState.CANCELED);
            }
        }

        eventMapper.updateEntityFromAdminRequest(updateEventAdminRequest, event);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }
}
