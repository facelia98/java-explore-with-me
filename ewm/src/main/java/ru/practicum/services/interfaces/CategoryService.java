package ru.practicum.services.interfaces;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.news.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> get(Integer from, Integer size);

    CategoryDto getById(Long id);

    CategoryDto addNewCategory(NewCategoryDto categoryDto);

    void deleteById(Long id);

    CategoryDto updateById(Long id, CategoryDto dto);

}