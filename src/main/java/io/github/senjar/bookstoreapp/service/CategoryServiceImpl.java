package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.book.BookDtoWithoutCategoryIds;
import io.github.senjar.bookstoreapp.dto.category.CategoryDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.BookMapper;
import io.github.senjar.bookstoreapp.mapper.CategoryMapper;
import io.github.senjar.bookstoreapp.model.Category;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import io.github.senjar.bookstoreapp.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public Page<CategoryDto> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto getById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find category by id: " + id)));
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Category with id "
                + id + " doesn't exist"));

        categoryMapper.updateCategoryFromDto(categoryDto, category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete category. Category with id "
                    + id + " doesn't exist");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public Page<BookDtoWithoutCategoryIds> findBooksByCategoriesId(Long id, Pageable pageable) {
        return bookRepository.findAllByCategoriesId(id, pageable)
                .map(bookMapper::toDtoWithoutCategories);
    }
}
