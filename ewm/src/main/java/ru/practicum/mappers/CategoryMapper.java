package ru.practicum.mappers;

import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.news.NewCategoryDto;
import ru.practicum.models.Category;

public class CategoryMapper {
    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName()).build();
    }

    public static Category toCategory(Long id, CategoryDto dto) {
        return Category.builder()
                .id(id)
                .name(dto.getName()).build();
    }

    public static Category toCategory(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName()).build();
    }

    public static Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName()).build();
    }
}
