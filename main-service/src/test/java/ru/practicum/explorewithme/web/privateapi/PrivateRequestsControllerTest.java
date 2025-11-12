package ru.practicum.explorewithme.web.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.service.privateapi.PrivateRequestService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateRequestsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PrivateRequestsControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private PrivateRequestService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void create_returns201() throws Exception {
        Mockito.when(service.create(anyLong(), anyLong()))
                .thenReturn(ParticipationRequestDto.builder().id(1L).build());

        mvc.perform(post("/users/{userId}/requests", 7L).param("eventId", "12"))
                .andExpect(status().isCreated());
    }

    @Test
    void cancel_returns200() throws Exception {
        Mockito.when(service.cancel(7L, 55L))
                .thenReturn(ParticipationRequestDto.builder().id(55L).build());

        mvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 7L, 55L))
                .andExpect(status().isOk());
    }

    @Test
    void list_returns200() throws Exception {
        Mockito.when(service.findUserRequests(7L))
                .thenReturn(List.of(ParticipationRequestDto.builder().id(1L).build()));

        mvc.perform(get("/users/{uid}/requests", 7L))
                .andExpect(status().isOk());
    }
}
