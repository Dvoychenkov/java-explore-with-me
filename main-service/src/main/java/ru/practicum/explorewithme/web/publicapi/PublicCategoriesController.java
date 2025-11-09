package ru.practicum.explorewithme.web.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.service.publicapi.PublicCategoryService;

import java.util.List;

/*
    Public: Категории
    Публичный API для работы с категориями
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoriesController {

    private final PublicCategoryService publicCategoryService;

    @GetMapping
    public List<CategoryDto> findAll(
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        log.info("Public list categories, from: {}, size: {}", from, size);

        return publicCategoryService.findAll(from, size);
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getById(@PathVariable("categoryId") Long categoryId) {
        log.info("Public get category by id: {}", categoryId);

        return publicCategoryService.getById(categoryId);
    }
}
