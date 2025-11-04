package ru.practicum.stats.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.stats.dto.NewHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class StatsControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    @Test
    void postHit_shouldReturn201() throws Exception {
        NewHitDto dto = NewHitDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip("192.168.0.1")
                .timestamp("2025-11-04 11:00:00")
                .build();

        mockMvc.perform(
                post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isCreated());

        Mockito.verify(statsService, Mockito.times(1)).saveHit(any(NewHitDto.class));
    }

    @Test
    void getStats_shouldReturnList() throws Exception {
        List<ViewStatsDto> data = Arrays.asList(
                ViewStatsDto.builder().app("app").uri("/events").hits(10L).build(),
                ViewStatsDto.builder().app("app").uri("/events/1").hits(4L).build()
        );

        Mockito.when(statsService.getStats(anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(data);

        mockMvc.perform(
                        get("/stats")
                                .param("start", "2025-11-01 00:00:00")
                                .param("end", "2025-11-05 00:00:00")
                                .param("unique", "false")
                                .param("uris", "/events", "/events/1")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].app", is("app")))
                .andExpect(jsonPath("$[0].uri", is("/events")))
                .andExpect(jsonPath("$[0].hits", is(10)));
    }
}
