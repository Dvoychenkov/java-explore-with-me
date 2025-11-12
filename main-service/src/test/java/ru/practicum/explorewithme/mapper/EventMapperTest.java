package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    void toFullDto_mapsNestedFields() {
        Event e = baseEvent();
        EventFullDto dto = mapper.toFullDto(e);
        assertThat(dto.getCategory().getId()).isEqualTo(100L);
        assertThat(dto.getCategory().getName()).isEqualTo("cat");
        assertThat(dto.getInitiator().getId()).isEqualTo(50L);
        assertThat(dto.getInitiator().getName()).isEqualTo("user");
        assertThat(dto.getLocation().getLat()).isEqualTo(1.1);
        assertThat(dto.getLocation().getLon()).isEqualTo(2.2);
        assertThat(dto.getState()).isEqualTo("PUBLISHED");
    }

    @Test
    void toShortDto_mapsNestedFields() {
        Event e = baseEvent();
        EventShortDto dto = mapper.toShortDto(e);
        assertThat(dto.getCategory().getId()).isEqualTo(100L);
        assertThat(dto.getInitiator().getId()).isEqualTo(50L);
    }

    private Event baseEvent() {
        Category c = new Category();
        c.setId(100L);
        c.setName("cat");
        User u = new User();
        u.setId(50L);
        u.setName("user");
        Location l = new Location();
        l.setLat(1.1);
        l.setLon(2.2);
        Event e = new Event();
        e.setId(1L);
        e.setTitle("t");
        e.setAnnotation("a");
        e.setDescription("d");
        e.setCategory(c);
        e.setInitiator(u);
        e.setLocation(l);
        e.setEventDate(LocalDateTime.now().plusDays(1));
        e.setCreatedOn(LocalDateTime.now());
        e.setState(EventState.PUBLISHED);
        e.setPaid(Boolean.FALSE);
        e.setParticipantLimit(0);
        e.setRequestModeration(Boolean.TRUE);
        return e;
    }
}
