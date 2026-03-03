package io.github.senjar.bookstoreapp.mapper;

import io.github.senjar.bookstoreapp.dto.book.CategoryDto;
import io.github.senjar.bookstoreapp.model.book.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateCategoryFromDto(CategoryDto categoryDto, @MappingTarget Category category);
}
