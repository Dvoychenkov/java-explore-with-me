package ru.practicum.explorewithme.service.publicapi;

import ru.practicum.explorewithme.dto.category.CategoryDto;

import java.util.List;

public interface PublicCategoryService {

    List<CategoryDto> findAll(int from, int size);

    CategoryDto getById(Long id);
}
