package ru.practicum.explorewithme.service.publicapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.EventSort;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.stats.StatsViewsService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static ru.practicum.explorewithme.domain.event.EventState.PUBLISHED;

@ExtendWith(MockitoExtension.class)
class PublicEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StatsViewsService statsViewsService;

    @Mock
    private ParticipationRequestRepository participationRequestRepository;

    @Spy
    private final EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @InjectMocks
    private PublicEventServiceImpl service;

    @Test
    void sortByViews_desc() {
        Event a = event(1L, "A", LocalDateTime.now().plusDays(1));
        Event b = event(2L, "B", LocalDateTime.now().plusDays(2));
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a, b)));

//        when(participationRequestRepository.findRequestsCountByEventIdsAndStatus(anyList(), any(RequestStatus.class)))
//                .thenReturn(List.of());

        Map<String, Long> views = new HashMap<>();
        views.put("/events/1", 100L);
        views.put("/events/2", 5L);
        when(statsViewsService.fetchViews(any(), any(), anyList(), anyBoolean())).thenReturn(views);

        PublicEventSearchCriteriaDto c = PublicEventSearchCriteriaDto.builder()
                .sort(EventSort.VIEWS).from(0).size(10).build();

        List<EventShortDto> out = service.search(c);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getId()).isEqualTo(1L);
        assertThat(out.get(0).getViews()).isEqualTo(100L);
        assertThat(out.get(1).getId()).isEqualTo(2L);
        assertThat(out.get(1).getViews()).isEqualTo(5L);
    }

    @Test
    void sortByEventDate_asc() {
        Event a = event(1L, "A", LocalDateTime.now().plusDays(2));
        Event b = event(2L, "B", LocalDateTime.now().plusDays(1));
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(b, a)));
        when(statsViewsService.fetchViews(any(), any(), anyList(), anyBoolean()))
                .thenReturn(new HashMap<>());

        PublicEventSearchCriteriaDto c = PublicEventSearchCriteriaDto.builder()
                .sort(EventSort.EVENT_DATE).from(0).size(10).build();

        List<EventShortDto> out = service.search(c);

        assertThat(out.get(0).getEventDate()).isBefore(out.get(1).getEventDate());
    }

    @Test
    void invalidRange_throwsBadRequest() {
        PublicEventSearchCriteriaDto c = PublicEventSearchCriteriaDto.builder()
                .rangeStart(LocalDateTime.of(2030, 1, 2, 0, 0))
                .rangeEnd(LocalDateTime.of(2029, 1, 2, 0, 0))
                .from(0).size(10).build();

        assertThatThrownBy(() -> service.search(c))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date must be after end date");
    }

    private Event event(Long id, String title, LocalDateTime date) {
        Event e = new Event();
        e.setId(id);
        e.setTitle(title);
        e.setAnnotation("ann");
        e.setDescription("desc");
        e.setEventDate(date);
        e.setCreatedOn(LocalDateTime.now());
        e.setState(PUBLISHED);
        Category cat = new Category();
        cat.setId(10L);
        cat.setName("cat");
        e.setCategory(cat);
        User u = new User();
        u.setId(5L);
        u.setName("user");
        e.setInitiator(u);
        Location l = new Location();
        l.setLat(1.0);
        l.setLon(2.0);
        e.setLocation(l);
        return e;
    }

    @Test
    void search_defaultSort_eventDateAsc_onlyAvailableTrue() {
        Event e1 = event(1L, LocalDateTime.now().plusDays(3));
        Event e2 = event(2L, LocalDateTime.now().plusDays(1));

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(e1, e2)));
        when(statsViewsService.fetchViews(any(), any(), anyList(), anyBoolean())).thenReturn(new HashMap<>());

        PublicEventSearchCriteriaDto c = PublicEventSearchCriteriaDto.builder()
                .onlyAvailable(Boolean.TRUE)
                .from(0).size(10)
                .build();

        List<EventShortDto> out = service.search(c);
        assertThat(out).hasSize(2);
        assertThat(out.get(0).getId()).isEqualTo(1L);
        assertThat(out.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getById_published_ok_setsConfirmedRequests() {
        Event e = event(5L, LocalDateTime.now().plusDays(1));
        when(eventRepository.findById(5L)).thenReturn(Optional.of(e));

        EventFullDto dto = service.getById(5L);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getState()).isEqualTo(PUBLISHED.name());
        assertThat(dto.getConfirmedRequests()).isNotNull();
    }

    @Test
    void getById_notPublished_throws404() {
        Event e = event(9L, LocalDateTime.now().plusDays(1));
        e.setState(EventState.PENDING);
        when(eventRepository.findById(9L)).thenReturn(Optional.of(e));

        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    private Event event(Long id, LocalDateTime date) {
        Category c = new Category();
        c.setId(10L);
        c.setName("cat");
        User u = new User();
        u.setId(7L);
        u.setName("user");
        u.setEmail("u@e.com");
        ru.practicum.explorewithme.domain.event.Location l = new ru.practicum.explorewithme.domain.event.Location();
        l.setLat(1.0);
        l.setLon(2.0);
        Event e = new Event();
        e.setId(id);
        e.setTitle("t");
        e.setAnnotation("a");
        e.setDescription("d");
        e.setCategory(c);
        e.setInitiator(u);
        e.setLocation(l);
        e.setState(PUBLISHED);
        e.setEventDate(date);
        e.setCreatedOn(LocalDateTime.now());
        e.setParticipantLimit(0);
        e.setPaid(Boolean.FALSE);
        e.setRequestModeration(Boolean.TRUE);
        return e;
    }
}
