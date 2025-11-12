package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void category_roundtrip() {
        Category c = categoryMapper.toEntity(new NewCategoryDto("music"));
        c.setId(5L);
        CategoryDto dto = categoryMapper.toDto(c);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("music");
    }

}
