package ru.practicum.explorewithme.service.adminapi;

import ru.practicum.explorewithme.dto.event.AdminEventSearchCriteriaDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> search(AdminEventSearchCriteriaDto adminEventSearchCriteriaDto);

    EventFullDto update(Long eventId, UpdateEventAdminRequest dto);
}
