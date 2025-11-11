package ru.practicum.explorewithme.service.privateapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.mapper.ParticipationRequestMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateEventRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private ParticipationRequestMapper mapper;

    @InjectMocks
    private PrivateEventRequestServiceImpl service;

    @Test
    void updateStatuses_limitReached_throwsConflict() {
        Event e = new Event();
        e.setId(10L);
        e.setParticipantLimit(1);
        e.setState(EventState.PUBLISHED);
        User u = new User();
        u.setId(7L);
        e.setInitiator(u);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(e));
        when(requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED)).thenReturn(1L);

        EventRequestStatusUpdateRequest req = EventRequestStatusUpdateRequest.builder()
                .status(RequestStatus.CONFIRMED)
                .requestIds(List.of(100L, 101L))
                .build();

        assertThatThrownBy(() -> service.updateStatuses(7L, 10L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Participant limit has been reached");
    }
}
