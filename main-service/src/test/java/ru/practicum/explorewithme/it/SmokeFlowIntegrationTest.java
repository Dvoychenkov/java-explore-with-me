package ru.practicum.explorewithme.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.event.EventStateAction;
import ru.practicum.explorewithme.dto.event.LocationDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.stats.client.StatsClientImpl;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ActiveProfiles("test")
class SmokeFlowIntegrationTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private StatsClientImpl statsClient;

    @Test
    void endToEnd_createPublishAndGetPublic() throws Exception {
        when(statsClient.saveHit(any())).thenReturn(ResponseEntity.ok().build());

        NewUserRequest newUser = NewUserRequest.builder().name("ann").email("ann@example.com").build();
        String userJson = objectMapper.writeValueAsString(newUser);

        String userCreated = mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        long userId = objectMapper.readTree(userCreated).get("id").asLong();

        NewCategoryDto newCat = NewCategoryDto.builder().name("music").build();
        String catJson = objectMapper.writeValueAsString(newCat);

        String catCreated = mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        long catId = objectMapper.readTree(catCreated).get("id").asLong();

        NewEventDto newEvent = NewEventDto.builder()
                .title("Rock Fest")
                .annotation("a".repeat(20))
                .description("d".repeat(20))
                .category(catId)
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(LocationDto.builder().lat(1.0).lon(2.0).build())
                .paid(Boolean.FALSE)
                .participantLimit(1)
                .requestModeration(Boolean.TRUE)
                .build();

        String eventJson = objectMapper.writeValueAsString(newEvent);

        String eventCreated = mvc.perform(post("/users/{uid}/events", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.state", equalTo("PENDING")))
                .andReturn().getResponse().getContentAsString();
        long eventId = objectMapper.readTree(eventCreated).get("id").asLong();

        UpdateEventAdminRequest publish = UpdateEventAdminRequest.builder()
                .stateAction(EventStateAction.PUBLISH_EVENT).build();
        String publishJson = objectMapper.writeValueAsString(publish);

        mvc.perform(patch("/admin/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", equalTo("PUBLISHED")));

        mvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo((int) eventId)))
                .andExpect(jsonPath("$.title", equalTo("Rock Fest")))
                .andExpect(jsonPath("$.state", equalTo("PUBLISHED")))
                .andExpect(jsonPath("$.category.name", equalTo("music")))
                .andExpect(jsonPath("$.confirmedRequests", notNullValue()));
    }
}
