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
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.AdminUpdateCommentStatusDto;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.service.adminapi.AdminCommentService;
import ru.practicum.stats.client.StatsClient;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCommentsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminCommentsControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AdminCommentService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void list_returns200() throws Exception {
        Mockito.when(service.listByEvent(2L, 0, 10))
                .thenReturn(Collections.singletonList(CommentDto.builder().id(1L).build()));

        mvc.perform(get("/admin/comments/events/{eventId}", 2L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void setStatus_returns200() throws Exception {
        AdminUpdateCommentStatusDto in = new AdminUpdateCommentStatusDto();
        in.setStatus(CommentStatus.HIDDEN);
        Mockito.when(service.setStatus(5L, ru.practicum.explorewithme.domain.comment.CommentStatus.HIDDEN))
                .thenReturn(CommentDto.builder().id(5L).status(CommentStatus.HIDDEN).build());

        mvc.perform(patch("/admin/comments/{commentId}/status", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/admin/comments/{commentId}", 9L))
                .andExpect(status().isNoContent());
    }
}
