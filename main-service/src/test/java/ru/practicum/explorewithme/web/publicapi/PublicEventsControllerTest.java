package ru.practicum.explorewithme.web.publicapi;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.publicapi.PublicEventService;
import ru.practicum.stats.client.StatsClient;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicEventsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PublicEventsControllerTest {

    private final MockMvc mvc;

    @MockBean
    private PublicEventService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void invalidSort_returns400() throws Exception {
        Mockito.when(service.search(any())).thenReturn(Collections.emptyList());

        mvc.perform(get("/events").param("sort", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void valid_returns200() throws Exception {
        Mockito.when(service.search(any())).thenReturn(Collections.emptyList());

        mvc.perform(get("/events").param("sort", "EVENT_DATE"))
                .andExpect(status().isOk());
    }
}
