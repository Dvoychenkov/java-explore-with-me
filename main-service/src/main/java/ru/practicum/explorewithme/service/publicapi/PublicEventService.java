package ru.practicum.explorewithme.service.publicapi;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;

import java.util.List;

public interface PublicEventService {

    // TODO переехать на EventSearchDto
    List<EventShortDto> search(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                               Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getById(Long eventId);
}
