package ru.practicum.explorewithme.service.publicapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.mapper.CategoryMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCategoryServiceImplTest {

    @Mock
    private CategoryRepository repository;

    @Spy
    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @InjectMocks
    private PublicCategoryServiceImpl service;

    @Test
    void findAll_paged_ok() {
        Category c = new Category();
        c.setId(1L);
        c.setName("music");
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(c)));

        List<CategoryDto> out = service.findAll(0, 10);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getName()).isEqualTo("music");
    }

    @Test
    void getById_ok() {
        Category c = new Category();
        c.setId(3L);
        c.setName("sport");
        when(repository.findById(3L)).thenReturn(Optional.of(c));

        CategoryDto dto = service.getById(3L);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("sport");
    }
}
