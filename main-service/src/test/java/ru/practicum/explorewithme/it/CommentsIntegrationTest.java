package ru.practicum.explorewithme.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.domain.comment.CommentStatus;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventState;
import ru.practicum.explorewithme.domain.event.Location;
import ru.practicum.explorewithme.domain.user.User;
import ru.practicum.explorewithme.domain.user.UserRepository;
import ru.practicum.explorewithme.dto.comment.AdminUpdateCommentStatusDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class CommentsIntegrationTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Test
    void fullFlow_add_list_hide_list_show_delete() throws Exception {
        User u = new User();
        u.setName("Bob");
        u.setEmail("b@b.com");
        u = userRepository.save(u);

        Category c = new Category();
        c.setName("films");
        c = categoryRepository.save(c);

        Event e = new Event();
        e.setTitle("t");
        e.setAnnotation("a".repeat(20));
        e.setDescription("d".repeat(20));
        e.setCategory(c);
        e.setInitiator(u);
        e.setEventDate(LocalDateTime.now().plusDays(1));
        e.setCreatedOn(LocalDateTime.now());
        e.setState(EventState.PUBLISHED);
        Location loc = new Location();
        loc.setLat(1.0);
        loc.setLon(2.0);
        e.setLocation(loc);
        e = eventRepository.save(e);


        mvc.perform(get("/events/{eventId}/comments", e.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));


        NewCommentDto in = new NewCommentDto();
        in.setText("Nice");
        String createResp = mvc.perform(post("/users/{userId}/events/{eventId}/comments", u.getId(), e.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long commentId = objectMapper.readTree(createResp).get("id").asLong();


        mvc.perform(get("/events/{eventId}/comments", e.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));


        AdminUpdateCommentStatusDto upd = new AdminUpdateCommentStatusDto();
        upd.setStatus(CommentStatus.HIDDEN);
        mvc.perform(patch("/admin/comments/{commentId}/status", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));


        mvc.perform(get("/events/{eventId}/comments", e.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));


        upd.setStatus(CommentStatus.PUBLISHED);
        mvc.perform(patch("/admin/comments/{commentId}/status", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));


        mvc.perform(delete("/users/{userId}/events/{eventId}/comments/{commentId}", u.getId(), e.getId(), commentId))
                .andExpect(status().isNoContent());


        mvc.perform(get("/events/{eventId}/comments", e.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
