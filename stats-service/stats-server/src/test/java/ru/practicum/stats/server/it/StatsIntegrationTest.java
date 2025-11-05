package ru.practicum.stats.server.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.stats.dto.NewHitDto;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class StatsIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Test
    void endToEnd_stats() throws Exception {
        NewHitDto hit1 = NewHitDto.builder()
                .app("ewm-main-service").uri("/events")
                .ip("10.0.0.1").timestamp("2025-11-04 12:00:00").build();

        NewHitDto hit2 = NewHitDto.builder()
                .app("ewm-main-service").uri("/events/1")
                .ip("10.0.0.2").timestamp("2025-11-04 12:05:00").build();

        NewHitDto hit3 = NewHitDto.builder()
                .app("ewm-main-service").uri("/events/1")
                .ip("10.0.0.2").timestamp("2025-11-04 12:06:00").build();

        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(hit1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(hit2)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(hit3)))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/stats")
                                .param("start", "2025-11-04 00:00:00")
                                .param("end", "2025-11-05 00:00:00")
                                .param("unique", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(
                        get("/stats")
                                .param("start", "2025-11-04 00:00:00")
                                .param("end", "2025-11-05 00:00:00")
                                .param("unique", "true")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uri==\"/events\")].hits").value(is(java.util.List.of(1))))
                .andExpect(jsonPath("$[?(@.uri==\"/events/1\")].hits").value(is(java.util.List.of(1))));
    }
}
