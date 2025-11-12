package ru.practicum.explorewithme.service.adminapi;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryRequest;

public interface AdminCategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(Long categoryId, UpdateCategoryRequest updateCategoryRequest);

    void delete(Long categoryId);
}
