package ru.practicum.explorewithme.service.privateapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.mapper.ParticipationRequestMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrivateEventRequestServiceImpl implements PrivateEventRequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> listEventRequests(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("User is not the initiator of the event");
        }

        return participationRequestRepository.findAllByEventId(eventId)
                .stream().map(participationRequestMapper::toDto).toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found by id: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new IllegalStateException("User is not the initiator of the event");
        }

        // TODO по-хорошему сделать покрасивее
        boolean confirm = "CONFIRMED".equalsIgnoreCase(eventRequestStatusUpdateRequest.getStatus());
        boolean reject = "REJECTED".equalsIgnoreCase(eventRequestStatusUpdateRequest.getStatus());
        if (!confirm && !reject) {
            throw new IllegalArgumentException("status must be CONFIRMED or REJECTED");
        }

        int limit = (event.getParticipantLimit() == null) ?
                0 :
                event.getParticipantLimit();
        long confirmed = participationRequestRepository.countByEventAndStatus(event, RequestStatus.CONFIRMED);

        // Лимит уже достигнут, а значит всё
        if (confirm && limit > 0 && confirmed >= limit) {
            throw new IllegalStateException("Participant limit has been reached");
        }

        List<ParticipationRequest> participationRequestsToUpdate = participationRequestRepository
                .findAllById(eventRequestStatusUpdateRequest.getRequestIds());

        List<ParticipationRequestDto> participationRequestsConfirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> participationRequestsRejectedDtos = new ArrayList<>();

        for (ParticipationRequest participationRequest : participationRequestsToUpdate) {
            if (!participationRequest.getEvent().getId().equals(eventId)) {
                throw new IllegalStateException("Request does not belong to this event");
            }
            if (participationRequest.getStatus() != RequestStatus.PENDING) {
                throw new IllegalStateException("Only PENDING requests can be updated");
            }

            if (confirm) {
                if (limit > 0 && confirmed >= limit) {
                    // Лимит уже исчерпан, а значит помещаем всё остальное в отклонённые
                    participationRequest.setStatus(RequestStatus.REJECTED);
                    participationRequestsRejectedDtos.add(participationRequestMapper.toDto(participationRequest));
                } else {
                    participationRequest.setStatus(RequestStatus.CONFIRMED);
                    confirmed++;
                    participationRequestsConfirmedDtos.add(participationRequestMapper.toDto(participationRequest));
                }
            } else {
                // Такие заявки сразу в отклонённые
                participationRequest.setStatus(RequestStatus.REJECTED);
                participationRequestsRejectedDtos.add(participationRequestMapper.toDto(participationRequest));
            }
        }

        participationRequestRepository.saveAll(participationRequestsToUpdate);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(participationRequestsConfirmedDtos)
                .rejectedRequests(participationRequestsRejectedDtos)
                .build();
    }
}
