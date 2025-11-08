package ru.practicum.explorewithme.service.privateapi;

import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventRequestService {

    List<ParticipationRequestDto> listEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId,
                                                  EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);
}
