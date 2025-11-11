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
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.adminapi.AdminCategoryService;
import ru.practicum.stats.client.StatsClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCategoriesController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AdminCategoriesControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private AdminCategoryService service;

    @MockBean
    private StatsClient statsClient;

    @Test
    void create_returns201() throws Exception {
        Mockito.when(service.create(any(NewCategoryDto.class)))
                .thenReturn(CategoryDto.builder().id(1L).name("c").build());

        mvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(NewCategoryDto.builder().name("c").build())))
                .andExpect(status().isCreated());
    }

    @Test
    void update_returns200() throws Exception {
        Mockito.when(service.update(eq(2L), any(UpdateCategoryRequest.class)))
                .thenReturn(CategoryDto.builder().id(2L).name("x").build());

        mvc.perform(patch("/admin/categories/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateCategoryRequest.builder().name("x").build())))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mvc.perform(delete("/admin/categories/{id}", 3L))
                .andExpect(status().isNoContent());
    }
}
