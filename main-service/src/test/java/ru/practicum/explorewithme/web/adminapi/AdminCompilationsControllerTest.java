package ru.practicum.explorewithme.web.adminapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.service.adminapi.AdminCompilationService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCompilationsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminCompilationsControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AdminCompilationService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void create_returns201() throws Exception {
        Mockito.when(service.create(any(NewCompilationDto.class)))
                .thenReturn(CompilationDto.builder().id(1L).title("t").pinned(Boolean.FALSE).events(List.of()).build());

        NewCompilationDto dto = NewCompilationDto.builder().title("t").pinned(Boolean.FALSE).events(List.of()).build();

        mvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void update_returns200() throws Exception {
        Mockito.when(service.update(eq(5L), any(UpdateCompilationRequest.class)))
                .thenReturn(CompilationDto.builder().id(5L).title("t2").pinned(Boolean.TRUE).events(List.of()).build());

        UpdateCompilationRequest dto = UpdateCompilationRequest.builder().title("t2").pinned(Boolean.TRUE).build();

        mvc.perform(patch("/admin/compilations/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/admin/compilations/{id}", 7L))
                .andExpect(status().isNoContent());
    }
}
