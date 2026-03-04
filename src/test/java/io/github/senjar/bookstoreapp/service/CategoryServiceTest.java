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
class CategoryServiceTest {

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
    void findAll_validPageable_returnsCategoryPage() {
        Category category = createCategory(1L, "Horror");
        CategoryDto categoryDto = createCategoryDto(1L, "Horror");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);

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
    void getById_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidCategoryId = 999L;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            categoryService.getById(invalidCategoryId);
        });
    }

    @Test
    @DisplayName("Should return a single category DTO when a valid ID is provided")
    public void getById_validId_returnsCategoryDto() {
        Long id = 1L;
        Category category = createCategory(id, "Horror");
        CategoryDto expected = createCategoryDto(id, "Horror");

        when(categoryMapper.toDto(category)).thenReturn(expected);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        CategoryDto actualDto = categoryService.getById(id);

        Assertions.assertEquals(actualDto.getName(), expected.getName());
        verify(categoryRepository).findById(id);
        verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("Should successfully map, save and return a new category DTO")
    void save_validRequest_returnsSavedCategoryDto() {
        CategoryRequestDto requestDto = createCategoryRequestDto("Horror");
        Category category = createCategory(1L, "Horror");
        CategoryDto expectedDto = createCategoryDto(1L, "Horror");

        when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expectedDto);

        CategoryDto actualDto = categoryService.save(requestDto);

        Assertions.assertEquals(requestDto.getName(), actualDto.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update existing category details when valid ID and data are provided")
    void update_validId_returnsUpdatedCategoryDto() {
        Long id = 1L;
        Category category = createCategory(id, "Horror");
        CategoryRequestDto updateDto = createCategoryRequestDto("Comedy");
        CategoryDto expected = createCategoryDto(id, "Comedy");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(expected);

        CategoryDto actual = categoryService.update(id, updateDto);

        Assertions.assertEquals(expected.getName(), actual.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Should prevent update and throw EntityNotFoundException for non-existing category ID")
    void update_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidCategoryId = 999L;
        CategoryRequestDto updateDto = createCategoryRequestDto("Comedy");

        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class, () -> {
            categoryService.update(invalidCategoryId, updateDto);
        });

        Assertions.assertTrue(exception.getMessage().contains("Category with id "
                + invalidCategoryId + " doesn't exist"));

        verify(categoryMapper, never()).updateCategoryFromDto(any(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully delete category when it exists in the database")
    void deleteById_validId_callsRepositoryDelete() {
        Long categoryId = 1L;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        categoryService.deleteById(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    @DisplayName("Should prevent deletion and throw EntityNotFoundException when category to delete is missing")
    void deleteById_invalidId_throwsEntityNotFoundException() {
        Long InvalidCategoryId = 999L;
        when(categoryRepository.existsById(InvalidCategoryId)).thenReturn(false);

        EntityNotFoundException exception =
                Assertions.assertThrows(EntityNotFoundException.class, () -> {
                categoryService.deleteById(InvalidCategoryId);
                });

        verify(categoryRepository, never()).deleteById(InvalidCategoryId);
    }

    @Test
    @DisplayName("Should return a list of books for a given category ID when category exists")
    void findBooksByCategoriesId_validId_returnsBooksPage() {
        Long categoryId = 1L;
        Book book = new Book();
        book.setTitle("Hyperion");
        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds();
        bookDto.setTitle("Hyperion");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.existsById(categoryId)).thenReturn(true);
        when(bookRepository.findAllByCategoriesId(categoryId, pageable)).thenReturn(bookPage);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(bookDto);

        Page<BookDtoWithoutCategoryIds> actualBookDto =
                categoryService.findBooksByCategoriesId(categoryId, pageable);

        Assertions.assertEquals(1, actualBookDto.getTotalElements());
        Assertions.assertEquals("Hyperion", actualBookDto.getContent().get(0).getTitle());
        verify(bookRepository).findAllByCategoriesId(categoryId, pageable);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when trying to fetch books for a non-existing category")
    public void findBooksByCategoriesId_invalidId_throwsEntityNotFoundException() {
        Long invalidId = 999L;

        when(bookRepository.existsById(invalidId)).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            categoryService.findBooksByCategoriesId(invalidId, PageRequest.of(0, 10));
        });
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Description for " + name);
        return category;
    }

    private CategoryDto createCategoryDto(Long id, String name) {
        CategoryDto dto = new CategoryDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription("Description for " + name);
        return dto;
    }

    private CategoryRequestDto createCategoryRequestDto(String name) {
        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName(name);
        dto.setDescription("Description for " + name);
        return dto;
    }

}
