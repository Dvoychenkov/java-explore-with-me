package ru.practicum.explorewithme.web.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.LocationDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.exception.ErrorHandler;
import ru.practicum.explorewithme.service.privateapi.PrivateEventService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateEventsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(ErrorHandler.class)
class PrivateEventsControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private PrivateEventService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void create_returns201() throws Exception {
        NewEventDto dto = NewEventDto.builder()
                .title("title")
                .annotation("anotation".repeat(20))
                .description("description".repeat(20))
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(LocationDto.builder().lat(1.0).lon(2.0).build())
                .paid(Boolean.FALSE)
                .participantLimit(0)
                .requestModeration(Boolean.TRUE)
                .build();
        Mockito.when(service.create(Mockito.eq(5L), any(NewEventDto.class)))
                .thenReturn(EventFullDto.builder().id(10L).title("t").build());

        mvc.perform(post("/users/{userId}/events", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void update_conflict_returns409() throws Exception {
        Mockito.when(service.update(Mockito.eq(5L), Mockito.eq(10L), any(UpdateEventUserRequest.class)))
                .thenThrow(new IllegalStateException("Cannot update a published event"));

        UpdateEventUserRequest req = UpdateEventUserRequest.builder().title("title").build();

        mvc.perform(patch("/users/{userId}/events/{eventId}", 5L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
