package ru.practicum.explorewithme.web.adminapi;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.event.AdminEventSearchCriteriaDto;
import ru.practicum.explorewithme.service.adminapi.AdminEventService;
import ru.practicum.stats.client.StatsClient;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminEventsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminEventsControllerTest {

    private final MockMvc mvc;

    @MockBean
    private AdminEventService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void getEvents_bindsCriteria_returns200() throws Exception {
        Mockito.when(service.search(any(AdminEventSearchCriteriaDto.class)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/admin/events")
                        .param("users", "1", "2")
                        .param("states", "PUBLISHED")
                        .param("categories", "10")
                        .param("rangeStart", "2025-01-01 00:00:00")
                        .param("rangeEnd", "2026-01-01 00:00:00")
                        .param("from", "0")
                        .param("size", "100"))
                .andExpect(status().isOk());
    }
}
