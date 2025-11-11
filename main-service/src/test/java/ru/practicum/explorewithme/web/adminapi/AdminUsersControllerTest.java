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
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;
import ru.practicum.explorewithme.service.adminapi.AdminUserService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUsersController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminUsersControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AdminUserService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void create_returns201() throws Exception {
        NewUserRequest req = NewUserRequest.builder().name("name").email("e@e.com").build();
        UserDto dto = UserDto.builder().id(1L).name("name").email("e@e.com").build();
        Mockito.when(service.create(Mockito.any())).thenReturn(dto);

        mvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/admin/users/{id}", 5L))
                .andExpect(status().isNoContent());
        Mockito.verify(service).delete(5L);
    }

    @Test
    void findByIds_param() throws Exception {
        List<UserDto> list = List.of(UserDto.builder().id(3L).name("u").email("u@u.com").build());
        Mockito.when(service.findAllByIds(Mockito.anyList())).thenReturn(list);

        mvc.perform(get("/admin/users").param("ids", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));
    }
}
