package ru.practicum.explorewithme.web.adminapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(
            @Valid @RequestBody NewCategoryDto newCategoryDto
    ) {
        log.info("Admin create, params => newCategoryDto: {}", newCategoryDto);

        return adminCategoryService.create(newCategoryDto);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto update(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest
    ) {
        log.info("Admin update category, params => categoryId: {}, updateCategoryRequest: {}",
                categoryId, updateCategoryRequest);

        return adminCategoryService.update(categoryId, updateCategoryRequest);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long categoryId
    ) {
        log.info("Admin delete, params => categoryId: {}", categoryId);

        adminCategoryService.delete(categoryId);
    }
}
