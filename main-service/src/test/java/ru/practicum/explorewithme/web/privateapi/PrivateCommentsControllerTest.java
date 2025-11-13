package ru.practicum.explorewithme.web.privateapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;
import ru.practicum.explorewithme.service.privateapi.PrivateCommentService;
import ru.practicum.stats.client.StatsClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateCommentsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class PrivateCommentsControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private PrivateCommentService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void add_returns201() throws Exception {
        NewCommentDto in = new NewCommentDto();
        in.setText("Hello");
        Mockito.when(service.add(1L, 2L, in)).thenReturn(CommentDto.builder().id(10L).build());

        mvc.perform(post("/users/{userId}/events/{eventId}/comments", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated());
    }

    @Test
    void update_returns200() throws Exception {
        UpdateCommentDto in = new UpdateCommentDto();
        in.setText("Updated");
        Mockito.when(service.update(1L, 10L, in)).thenReturn(CommentDto.builder().id(10L).build());

        mvc.perform(patch("/users/{userId}/events/{eventId}/comments/{commentId}", 1L, 2L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/users/{userId}/events/{eventId}/comments/{commentId}", 1L, 2L, 10L))
                .andExpect(status().isNoContent());
    }
}
