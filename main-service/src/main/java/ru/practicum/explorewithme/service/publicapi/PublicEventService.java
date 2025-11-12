package ru.practicum.explorewithme.service.publicapi;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> search(PublicEventSearchCriteriaDto publicEventSearchCriteriaDto);

    EventFullDto getById(Long eventId);
}
