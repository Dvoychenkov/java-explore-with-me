package ru.practicum.explorewithme.service.privateapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.LocationDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @InjectMocks
    private PrivateEventServiceImpl service;

    @Test
    void create_invalidDate_throws400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category(10L)));
        NewEventDto dto = NewEventDto.builder()
                .title("t").annotation("a".repeat(20)).description("d".repeat(20))
                .category(10L).eventDate(LocalDateTime.now().plusMinutes(10))
                .location(LocationDto.builder().lat(1.0).lon(2.0).build())
                .build();

        assertThatThrownBy(() -> service.create(1L, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_changeCategoryAndLocation_ok() {
        Event e = baseEvent();
        when(eventRepository.findByIdAndInitiatorId(100L, 1L)).thenReturn(Optional.of(e));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(category(20L)));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

        UpdateEventUserRequest req = UpdateEventUserRequest.builder()
                .category(20L)
                .location(LocationDto.builder().lat(5.0).lon(6.0).build())
                .build();

        EventFullDto dto = service.update(1L, 100L, req);
        assertThat(dto.getCategory().getId()).isEqualTo(20L);
        assertThat(dto.getLocation().getLat()).isEqualTo(5.0);
    }

    @Test
    void cancel_published_conflict() {
        Event e = baseEvent();
        e.setState(EventState.PUBLISHED);
        when(eventRepository.findByIdAndInitiatorId(100L, 1L)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> service.cancel(1L, 100L))
                .isInstanceOf(IllegalStateException.class);
    }

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user");
        u.setEmail("u@e.com");
        return u;
    }

    private Category category(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("cat");
        return c;
    }

    private Event baseEvent() {
        Event e = new Event();
        e.setId(100L);
        e.setState(EventState.PENDING);
        e.setInitiator(user(1L));
        e.setCategory(category(10L));
        e.setEventDate(LocalDateTime.now().plusDays(2));
        ru.practicum.explorewithme.domain.event.Location l = new ru.practicum.explorewithme.domain.event.Location();
        l.setLat(1.0);
        l.setLon(2.0);
        e.setLocation(l);
        return e;
    }
}
