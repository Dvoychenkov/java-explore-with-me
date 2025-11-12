package ru.practicum.explorewithme.service.privateapi;

import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateRequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> findUserRequests(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);
}
