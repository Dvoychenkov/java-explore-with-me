package ru.practicum.explorewithme.service.adminapi;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.domain.category.Category;
import ru.practicum.explorewithme.domain.category.CategoryRepository;
import ru.practicum.explorewithme.domain.event.Event;
import ru.practicum.explorewithme.domain.event.EventRepository;
import ru.practicum.explorewithme.domain.event.EventSpecifications;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategoryDto;
import ru.practicum.explorewithme.dto.category.UpdateCategoryRequest;
import ru.practicum.explorewithme.mapper.CategoryMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toEntity(newCategoryDto);

        Category saved;
        try {
            saved = categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Category name must be unique");
        }

        return categoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto update(Long categoryId, UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + categoryId));

        categoryMapper.updateEntity(updateCategoryRequest, category);

        Category updated;
        try {
            updated = categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Category name must be unique");
        }

        return categoryMapper.toDto(updated);
    }

    @Override
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + categoryId));

        // Если к категории привязаны события — это конфликтная ситуация
        Specification<Event> eventSpecification = EventSpecifications.hasEventsInCategory(categoryId);
        boolean hasEvents = eventRepository.exists(eventSpecification);
        if (hasEvents) {
            throw new IllegalStateException("Cannot delete category with linked events");
        }

        categoryRepository.delete(category);
    }
}
