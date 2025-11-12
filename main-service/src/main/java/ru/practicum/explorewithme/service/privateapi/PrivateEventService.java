package ru.practicum.explorewithme.service.privateapi;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;

import java.util.List;

public interface PrivateEventService {

    EventFullDto create(Long userId, NewEventDto dto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest dto);

    EventFullDto cancel(Long userId, Long eventId);
}
