package io.github.senjar.bookstoreapp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.senjar.bookstoreapp.dto.book.BookDtoWithoutCategoryIds;
import io.github.senjar.bookstoreapp.dto.book.CategoryDto;
import io.github.senjar.bookstoreapp.dto.book.CategoryRequestDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.BookMapper;
import io.github.senjar.bookstoreapp.mapper.CategoryMapper;
import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.model.book.Category;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import io.github.senjar.bookstoreapp.repository.book.CategoryRepository;
import io.github.senjar.bookstoreapp.service.impl.CategoryServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Business Logic Tests")
public class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Test
    @DisplayName("Should return a paginated list of all categories mapped to DTOs")
    public void findAll_ValidPageable_ReturnsAllCategories() {
        Category category = new Category();
        category.setName("Horror");
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Horror");

        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, 1);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        Page<CategoryDto> actualPage = categoryService.findAll(pageable);
        List<CategoryDto> actualContent = actualPage.getContent();

        Assertions.assertEquals(categoryDto.getName(), actualContent.get(0).getName());
        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when category ID is not found in database")
    public void getById_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidCategoryId = -10L;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            categoryService.getById(invalidCategoryId);
        });
    }

    @Test
    @DisplayName("Should return a single category DTO when a valid ID is provided")
    public void getById_ValidId_ReturnsCategoryById() {
        Category category = new Category();
        Long categoryId = 1L;
        category.setId(categoryId);
        category.setName("Horror");

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Horror");

        when(categoryMapper.toDto(category)).thenReturn(categoryDto);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryDto actualDto = categoryService.getById(categoryId);

        Assertions.assertEquals(actualDto.getName(), categoryDto.getName());
    }

    @Test
    @DisplayName("Should successfully map, save and return a new category DTO")
    public void save_ValidRequest_ReturnsSavedCategory() {
        Category category = new Category();
        category.setName("Horror");
        category.setId(1L);

        CategoryRequestDto categoryDto = new CategoryRequestDto();
        categoryDto.setName("Horror");

        CategoryDto expectedDto = new CategoryDto();
        expectedDto.setName("Horror");

        when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expectedDto);

        CategoryDto actualDto = categoryService.save(categoryDto);

        Assertions.assertEquals(categoryDto.getName(), actualDto.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update existing category details when valid ID and data are provided")
    public void update_ValidId_UpdatesSingeCategory() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setName("Horror");
        category.setId(categoryId);

        CategoryRequestDto updateDto = new CategoryRequestDto();
        updateDto.setName("Comedy");

        CategoryDto expected = new CategoryDto();
        expected.setName("Comedy");
        expected.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryDto actual = categoryService.update(categoryId, updateDto);

        Assertions.assertEquals(expected.getName(), actual.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Should prevent update and throw EntityNotFoundException for non-existing category ID")
    public void update_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidCategoryId = -10L;

        CategoryRequestDto updateDto = new CategoryRequestDto();
        updateDto.setName("Comedy");

        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            categoryService.update(invalidCategoryId, updateDto);
        });

        Assertions.assertTrue(exception.getMessage().contains("Category with id "
                + invalidCategoryId + " doesn't exist"));

        verify(categoryMapper, never()).updateCategoryFromDto(any(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully delete category when it exists in the database")
    public void deleteById_ValidId_DeletesCategory() {
        Long categoryId = 1L;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        categoryService.deleteById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("Should prevent deletion and throw EntityNotFoundException when category to delete is missing")
    public void deleteById_InvalidId_ThrowsEntityNotFoundException() {
        Long InvalidCategoryId = -10L;
        when(categoryRepository.existsById(InvalidCategoryId)).thenReturn(false);

        EntityNotFoundException exception =
                Assertions.assertThrows(EntityNotFoundException.class, () -> {
                categoryService.deleteById(InvalidCategoryId);
                });
        Assertions.assertTrue(exception.getMessage().contains(
                "Can't delete category. Category with id "
                + InvalidCategoryId + " doesn't exist"));

        verify(categoryRepository, never()).deleteById(InvalidCategoryId);
    }

    @Test
    @DisplayName("Should return a list of books for a given category ID when category exists")
    public void findBooksByCategoriesId_ValidId_ReturnsBooks() {
        Long categoryId = 10L;
        Category category = new Category();
        category.setName("Horror");
        category.setId(categoryId);

        Book book = new Book();
        book.setTitle("Hyperion");
        book.setCategories(Set.of(category));

        BookDtoWithoutCategoryIds expectedDto = new BookDtoWithoutCategoryIds();
        expectedDto.setTitle("Hyperion");

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> bookList = List.of(book);
        Page<Book> bookPage = new PageImpl<>(bookList, pageable, 1);

        when(bookRepository.existsById(categoryId)).thenReturn(true);
        when(bookRepository.findAllByCategoriesId(categoryId, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(expectedDto);

        Page<BookDtoWithoutCategoryIds> actualBookDto =
                categoryService.findBooksByCategoriesId(categoryId, pageable);
        List<BookDtoWithoutCategoryIds> actualContent = actualBookDto.getContent();

        Assertions.assertEquals(1, actualContent.size());
        Assertions.assertEquals(expectedDto.getTitle(), actualContent.get(0).getTitle());
        verify(bookRepository).findAllByCategoriesId(categoryId, pageable);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when trying to fetch books for a non-existing category")
    public void findBooksByCategoriesId_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidId = -10L;

        when(bookRepository.existsById(invalidId)).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            categoryService.findBooksByCategoriesId(invalidId, PageRequest.of(0, 10));
        });
    }

}
