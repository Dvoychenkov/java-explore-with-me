package ru.practicum.explorewithme.web.publicapi;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.service.publicapi.PublicCompilationService;
import ru.practicum.stats.client.StatsClient;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCompilationsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicCompilationsControllerTest {

    private final MockMvc mvc;

    @MockBean
    private PublicCompilationService compilationService;

    @MockBean
    private StatsClient statsClient;

    @Test
    void compilationList_returns200() throws Exception {
        Mockito.when(compilationService.findAll(Boolean.FALSE, 0, 10))
                .thenReturn(Collections.singletonList(CompilationDto.builder().id(1L).title("t").build()));
        mvc.perform(get("/compilations")).andExpect(status().isOk());
    }
}
