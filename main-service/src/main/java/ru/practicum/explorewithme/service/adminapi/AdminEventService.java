package ru.practicum.explorewithme.service.adminapi;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {

    // TODO Добавить search DTO
    List<EventFullDto> search(List<Long> users, List<String> states, List<Long> categories,
                              String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto update(Long eventId, UpdateEventAdminRequest dto);
}
