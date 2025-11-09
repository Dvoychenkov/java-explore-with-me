package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryRequest;
import ru.practicum.explorewithme.service.adminapi.AdminCategoryService;

/*
    Admin: Категории
    API для работы с категориями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoriesController {

    private final AdminCategoryService adminCategoryService;

    @PostMapping
    public CategoryDto create(
            @Valid @RequestBody NewCategoryDto newCategoryDto
    ) {
        log.info("Admin create category : {}", newCategoryDto);

        return adminCategoryService.create(newCategoryDto);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto update(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest
    ) {
        log.info("Admin update category id: {}, category: {}", categoryId, updateCategoryRequest);

        return adminCategoryService.update(categoryId, updateCategoryRequest);
    }

    @DeleteMapping("/{categoryId}")
    public void delete(
            @PathVariable Long categoryId
    ) {
        log.info("Admin delete category id: {}", categoryId);

        adminCategoryService.delete(categoryId);
    }
}
