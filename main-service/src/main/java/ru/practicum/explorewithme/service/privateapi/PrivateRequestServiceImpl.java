package ru.practicum.explorewithme.service.privateapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.mapper.ParticipationRequestMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        // Проверка на ограничения при создании евента
        if (event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("Requester cannot be the event initiator");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot request participation for a non-published event");
        }
        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new IllegalStateException("Duplicate participation request");
        }

        long confirmed = participationRequestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0
                && confirmed >= event.getParticipantLimit()) {
            throw new IllegalStateException("Participant limit has been reached");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(requester);
        participationRequest.setEvent(event);
        participationRequest.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        // Учёт модерации евента
        boolean requiresModeration = Boolean.TRUE.equals(event.getRequestModeration());
        if (!requiresModeration || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            participationRequest.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest saved = participationRequestRepository.save(participationRequest);
        return participationRequestMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> findUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByRequesterId(userId);
        return participationRequestMapper.toDtoList(participationRequests);
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest participationRequest = participationRequestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found by id: " + requestId));

        participationRequest.setStatus(RequestStatus.CANCELED);

        ParticipationRequest saved = participationRequestRepository.save(participationRequest);
        return participationRequestMapper.toDto(saved);
    }
}
