package ru.practicum.explorewithme.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.dto.category.CategoryDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    // TODO
    //    @Mapping(target = "id", source = "id")
    CategoryDto toDto(Category category);
}