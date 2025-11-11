package ru.practicum.explorewithme.web.publicapi;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.service.publicapi.PublicCategoryService;
import ru.practicum.stats.client.StatsClient;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCategoriesController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicCategoriesControllerTest {

    private final MockMvc mvc;

    @MockBean
    private PublicCategoryService categoryService;

    @MockBean
    private StatsClient statsClient;

    @Test
    void categoriesList_returns200() throws Exception {
        Mockito.when(categoryService.findAll(0, 10))
                .thenReturn(Collections.singletonList(CategoryDto.builder().id(1L).name("c").build()));
        mvc.perform(get("/categories")).andExpect(status().isOk());
    }
}
