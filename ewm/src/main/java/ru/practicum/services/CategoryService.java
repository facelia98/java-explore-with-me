package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.news.NewCategoryDto;
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CategoryMapper;
import ru.practicum.models.Category;
import ru.practicum.repositories.CategoryRepository;
import ru.practicum.repositories.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public List<CategoryDto> get(Integer from, Integer size) {
        return categoryRepository
                .findAll(PageRequest.of(from, size)).get()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getById(Long id) {
        if (!categoryRepository.existsById(id)) {
            log.error("Category not found for id = {}", id);
            throw new NotFoundException("Category not found for id = " + id);
        }
        return CategoryMapper.toCategoryDto(categoryRepository.getById(id));
    }

    public CategoryDto addNewCategory(NewCategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()) != null) {
            log.error("Duplicate category name!");
            throw new Conflict("Duplicate category name!");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(categoryDto)));
    }

    public void deleteById(Long id) {
        if (!eventRepository.findByCategoryId(id).isEmpty()) {
            log.warn("Category being deleted contains events");
            throw new Conflict("Category being deleted contains events!");
        }
        categoryRepository.deleteById(id);
    }

    public CategoryDto updateById(Long id, CategoryDto dto) {
        if (dto.getName() != null) {
            Category c = categoryRepository.findByName(dto.getName());
            if (c != null && !c.getId().equals(id)) {
                log.error("Duplicate category name!");
                throw new Conflict("Duplicate category name!");
            }
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(id, dto)));
    }
}
