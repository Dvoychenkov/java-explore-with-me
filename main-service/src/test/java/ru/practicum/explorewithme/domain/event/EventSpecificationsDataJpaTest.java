package ru.practicum.explorewithme.domain.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.ParticipationRequestRepository;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventSpecificationsDataJpaTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ParticipationRequestRepository requestRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void onlyAvailable_respectsLimit() {
        Category cat = categoryRepository.save(cat("c1"));
        User u = userRepository.save(user("u1"));

        Event unlimited = event("e0", cat, u, 0);
        Event withSlots = event("e1", cat, u, 2);
        Event full = event("e2", cat, u, 1);

        eventRepository.saveAll(List.of(unlimited, withSlots, full));

        ParticipationRequest pr = new ParticipationRequest();
        pr.setEvent(full);
        pr.setRequester(u);
        pr.setStatus(RequestStatus.CONFIRMED);
        pr.setCreated(LocalDateTime.now());
        requestRepository.save(pr);

        Specification<Event> spec = EventSpecifications.onlyAvailable();

        List<Event> result = eventRepository.findAll(spec);
        assertThat(result).extracting(Event::getTitle).contains("e0", "e1").doesNotContain("e2");
    }

    @Test
    void publicSearch_filtersByStateCategoryPaidAndText() {
        Category cat = categoryRepository.save(cat("music"));
        Category cat2 = categoryRepository.save(cat("sport"));
        User u = userRepository.save(user("u"));

        Event a = event("Rock fest", cat, u, 0);
        a.setAnnotation("Best music");
        a.setDescription("Open air");
        a.setPaid(Boolean.FALSE);
        a.setState(EventState.PUBLISHED);

        Event b = event("Football", cat2, u, 0);
        b.setAnnotation("Ball game");
        b.setDescription("Stadium");
        b.setPaid(Boolean.TRUE);
        b.setState(EventState.PUBLISHED);

        Event c = event("Draft", cat, u, 0);
        c.setAnnotation("Unpub");
        c.setDescription("Hidden");
        c.setPaid(Boolean.FALSE);
        c.setState(EventState.PENDING);

        eventRepository.saveAll(List.of(a, b, c));

        PublicEventSearchCriteriaDto publicEventSearchCriteriaDto = PublicEventSearchCriteriaDto.builder()
                .text("music")
                .categories(List.of(cat.getId()))
                .paid(Boolean.FALSE)
                .rangeStart(LocalDateTime.now().minusYears(1))
                .rangeEnd(LocalDateTime.now().plusYears(1))
                .build();
        Specification<Event> spec = EventSpecifications.publicSearch(publicEventSearchCriteriaDto);

        List<Event> result = eventRepository.findAll(spec);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Rock fest");
    }

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    private User user(String name) {
        User u = new User();
        u.setName(name);
        u.setEmail(name + "@ex.com");
        return u;
    }

    private Event event(String title, Category cat, User u, int limit) {
        Event e = new Event();
        e.setTitle(title);
        e.setAnnotation("ann");
        e.setDescription("desc");
        e.setCategory(cat);
        e.setInitiator(u);
        e.setParticipantLimit(limit);
        e.setRequestModeration(Boolean.TRUE);
        e.setPaid(Boolean.FALSE);
        e.setState(EventState.PUBLISHED);
        e.setEventDate(LocalDateTime.now().plusDays(10));
        e.setCreatedOn(LocalDateTime.now());
        Location l = new Location();
        l.setLat(1.0);
        l.setLon(2.0);
        e.setLocation(l);
        return e;
    }
}
