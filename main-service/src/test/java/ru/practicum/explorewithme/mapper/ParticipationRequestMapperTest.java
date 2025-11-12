package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipationRequestMapperTest {

    private final ParticipationRequestMapper mapper = Mappers.getMapper(ParticipationRequestMapper.class);

    @Test
    void toDto_mapsIdsAndStatus() {
        User u = new User();
        u.setId(7L);
        Event e = new Event();
        e.setId(9L);
        ParticipationRequest pr = new ParticipationRequest();
        pr.setId(11L);
        pr.setRequester(u);
        pr.setEvent(e);
        pr.setStatus(RequestStatus.CONFIRMED);
        pr.setCreated(LocalDateTime.now());

        ParticipationRequestDto dto = mapper.toDto(pr);
        assertThat(dto.getId()).isEqualTo(11L);
        assertThat(dto.getRequester()).isEqualTo(7L);
        assertThat(dto.getEvent()).isEqualTo(9L);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }
}
