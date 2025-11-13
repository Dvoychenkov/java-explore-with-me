package ru.practicum.explorewithme.web.publicapi;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.service.publicapi.PublicCommentService;
import ru.practicum.stats.client.StatsClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCommentsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PublicCommentsControllerTest {

    private final MockMvc mvc;

    @MockBean
    private PublicCommentService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void list_returns200() throws Exception {
        Mockito.when(service.listByEvent(1L, 0, 10))
                .thenReturn(Collections.singletonList(CommentDto.builder().id(1L).build()));

        mvc.perform(get("/events/{eventId}/comments", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
