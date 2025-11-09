package ru.practicum.explorewithme.service.privateapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.util.QueryUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventMapper eventMapper;

    // Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
    private static final long MIN_HOURS_AHEAD = 2L;

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + newEventDto.getCategory()));

        validateEventDate(newEventDto.getEventDate());

        Event entity = eventMapper.toEntity(newEventDto, initiator, category);
        entity.setState(EventState.PENDING);
        entity.setCreatedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        entity.setPublishedOn(null);

        Event saved = eventRepository.save(entity);
        EventFullDto eventFullDto = eventMapper.toFullDto(saved);

        long confirmed = participationRequestRepository.countByEventAndStatus(saved, RequestStatus.CONFIRMED);
        eventFullDto.setConfirmedRequests(confirmed);

        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size) {
        Sort idDescSort = Sort.by("id").ascending();
        Pageable offsetLimit = QueryUtils.offsetLimit(from, size, idDescSort);

        return eventRepository.findAllByInitiatorId(userId, offsetLimit)
                .map(eventMapper::toShortDto)
                .getContent();
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        // Редактирование возможно только если не опубликовано
        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot update a published event");
        }

        // Обработка смены состояния
        if (updateEventUserRequest.getStateAction() != null) {
            if ("CANCEL_REVIEW".equalsIgnoreCase(updateEventUserRequest.getStateAction())) {
                event.setState(EventState.CANCELED);
            } else if ("SEND_TO_REVIEW".equalsIgnoreCase(updateEventUserRequest.getStateAction())) {
                event.setState(EventState.PENDING);
            }
        }

        eventMapper.updateEntity(updateEventUserRequest, event);

        // Ручное обновление оставшихся полей
        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + updateEventUserRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = new Location();
            location.setLat(updateEventUserRequest.getLocation().getLat());
            location.setLon(updateEventUserRequest.getLocation().getLon());
            event.setLocation(location);
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    public EventFullDto cancel(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + eventId));

        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot cancel a published event");
        }

        event.setState(EventState.CANCELED);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    private void validateEventDate(LocalDateTime eventDate) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(MIN_HOURS_AHEAD);
        if (eventDate.isBefore(threshold)) {
            throw new IllegalArgumentException("eventDate must be at least " + MIN_HOURS_AHEAD + " hours in the future");
        }
    }
}
