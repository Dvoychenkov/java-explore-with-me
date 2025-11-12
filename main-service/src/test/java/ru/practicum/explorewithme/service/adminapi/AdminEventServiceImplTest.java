package ru.practicum.explorewithme.service.adminapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventStateAction;
import ru.practicum.explorewithme.dto.event.LocationDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

//    @Mock
//    private UserRepository userRepository;

    @Mock
    private ParticipationRequestRepository participationRequestRepository;

    @Spy
    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @InjectMocks
    private AdminEventServiceImpl service;

    @Test
    void publish_fromPending_ok() {
        Event e = new Event();
        e.setId(1L);
        e.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

        UpdateEventAdminRequest req = UpdateEventAdminRequest.builder().stateAction(EventStateAction.PUBLISH_EVENT).build();
        EventFullDto dto = service.update(1L, req);

        org.assertj.core.api.Assertions.assertThat(dto.getState()).isEqualTo("PUBLISHED");
    }

    @Test
    void publish_fromPublished_conflict() {
        Event e = new Event();
        e.setId(2L);
        e.setState(EventState.PUBLISHED);
        when(eventRepository.findById(2L)).thenReturn(Optional.of(e));

        UpdateEventAdminRequest req = UpdateEventAdminRequest.builder().stateAction(EventStateAction.PUBLISH_EVENT).build();

        assertThatThrownBy(() -> service.update(2L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only events in state 'PENDING' can be published");
    }

    @Test
    void reject_fromPublished_conflict() {
        Event e = new Event();
        e.setId(3L);
        e.setState(EventState.PUBLISHED);
        when(eventRepository.findById(3L)).thenReturn(Optional.of(e));

        UpdateEventAdminRequest req = UpdateEventAdminRequest.builder().stateAction(EventStateAction.REJECT_EVENT).build();

        assertThatThrownBy(() -> service.update(3L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Events in state 'PUBLISHED' can't be rejected");
    }

    @Test
    void update_eventDate_inPast_badRequest() {
        Event e = new Event();
        e.setId(4L);
        e.setState(EventState.PENDING);
        when(eventRepository.findById(4L)).thenReturn(Optional.of(e));

        UpdateEventAdminRequest req = UpdateEventAdminRequest.builder()
                .eventDate(LocalDateTime.now().minusHours(1))
                .build();

        assertThatThrownBy(() -> service.update(4L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventDate must be at least");
    }

    @Test
    void update_fieldsWithoutStateAction_ok() {
        Event e = new Event();
        e.setId(1L);
        e.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(e));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0, Event.class));

        UpdateEventAdminRequest req = UpdateEventAdminRequest.builder()
                .paid(Boolean.TRUE)
                .participantLimit(10)
                .requestModeration(Boolean.FALSE)
                .location(LocationDto.builder().lat(3.3).lon(4.4).build())
                .eventDate(LocalDateTime.now().plusHours(2))
                .build();

        EventFullDto dto = service.update(1L, req);
        assertThat(dto.getParticipantLimit()).isEqualTo(10);
        assertThat(dto.getLocation().getLat()).isEqualTo(3.3);
        assertThat(dto.getPaid()).isTrue();
        assertThat(dto.getRequestModeration()).isFalse();
    }
}
