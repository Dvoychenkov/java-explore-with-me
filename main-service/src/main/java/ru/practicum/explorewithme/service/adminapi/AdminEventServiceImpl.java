package ru.practicum.explorewithme.service.adminapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.event.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explorewithme.util.DateTimeUtils.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> search(List<Long> users, List<String> states, List<Long> categories,
                                     String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = fromString(rangeStart);
        LocalDateTime end = fromString(rangeEnd);

        Specification<Event> eventSpecification = EventSpecifications.adminSearch(users, states, categories, start, end);

        Sort idDescSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, idDescSort);

        return eventRepository.findAll(eventSpecification, offsetLimit)
                .map(eventMapper::toFullDto)
                .getContent();
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        // TODO частично вынести в маппер
        // Частичные изменения полей
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        LocalDateTime eventDate = updateEventAdminRequest.getEventDate();
        if (eventDate != null) {
            // Дата начала изменяемого события должна быть не ранее чем за час от даты публикации
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("eventDate must be at least 1 hour in the future");
            }
            event.setEventDate(eventDate);
        }
        if (updateEventAdminRequest.getLocation() != null) {
            Location location = new Location();
            location.setLat(updateEventAdminRequest.getLocation().getLat());
            location.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLocation(location);
        }

        String stateAction = updateEventAdminRequest.getStateAction();
        if (stateAction != null) {
            if ("PUBLISH_EVENT".equalsIgnoreCase(stateAction)) {
                if (event.getState() != EventState.PENDING) {
                    throw new IllegalStateException("Only PENDING events can be published");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equalsIgnoreCase(stateAction)) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new IllegalStateException("Cannot reject a published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }
}
