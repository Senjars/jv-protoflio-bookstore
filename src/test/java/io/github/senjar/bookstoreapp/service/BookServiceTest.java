package io.github.senjar.bookstoreapp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.senjar.bookstoreapp.dto.book.BookDto;
import io.github.senjar.bookstoreapp.dto.book.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.dto.book.CreateBookRequestDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.BookMapper;
import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.model.book.Category;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import io.github.senjar.bookstoreapp.repository.book.BookSpecificationBuilder;
import io.github.senjar.bookstoreapp.repository.book.CategoryRepository;
import io.github.senjar.bookstoreapp.service.impl.BookServiceImpl;
import java.math.BigDecimal;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("Book Service Business Logic Tests")
public class BookServiceTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Test
    @DisplayName("Should successfully retrieve a book when a valid ID is provided")
    public void getBookById_WithValidBookId_ReturnsValidBook() {
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Hyperion");
        book.setAuthor("Dan Simmons");

        BookDto expectedDto = new BookDto();
        expectedDto.setId(bookId);
        expectedDto.setTitle("Hyperion");
        expectedDto.setAuthor("Dan Simmons");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expectedDto);

        BookDto actual = bookService.getBookById(bookId);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(actual.getTitle(), expectedDto.getTitle());
        Assertions.assertEquals(actual.getAuthor(), expectedDto.getAuthor());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when attempting to get a book with a non-existing ID")
    public void getBookById_WithInvalidBookId_ThrowsEntityNotFoundException() {
        Long invalidBookId = -10L;
        when(bookRepository.findById(invalidBookId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            bookService.getBookById(invalidBookId);
        });
    }

    @Test
    @DisplayName("Should correctly update and return a book when valid data and ID are provided")
    public void update_ValidRequestDtoValidId_ReturnsUpdatedBook() {
        Long bookId = 1L;
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Hyperion");
        existingBook.setAuthor("Dan Simmons");
        existingBook.setPrice(new BigDecimal("19.99"));

        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setTitle("Dan Simmons");
        requestDto.setPrice(new BigDecimal("15.99"));

        BookDto expectedDto = new BookDto();
        expectedDto.setId(bookId);
        expectedDto.setTitle("Hyperion");
        expectedDto.setAuthor("Dan Simmons");
        expectedDto.setPrice(new BigDecimal("15.99"));

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);
        when(bookMapper.toDto(existingBook)).thenReturn(expectedDto);

        BookDto actual = bookService.update(requestDto, bookId);

        Assertions.assertEquals(expectedDto.getAuthor(), actual.getAuthor());
        Assertions.assertEquals(expectedDto.getPrice(), actual.getPrice());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when attempting to update a non-existing book")
    public void update_ValidRequestDtoInvalidId_ThrowsEntityNotFoundException() {
        Long invalidBookId = -1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto();

        when(bookRepository.findById(invalidBookId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(requestDto, invalidBookId);
        });
    }

    @Test
    @DisplayName("Should successfully delete a book by ID when the book exists")
    public void delete_ValidId_DeletesBook() {
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);

        bookService.deleteById(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException and prevent deletion when book ID does not exist")
    public void delete_InvalidId_ThrowsEntityNotFoundException() {
        Long invalidBookId = -10L;
        when(bookRepository.existsById(invalidBookId)).thenReturn(false);

        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () -> {
            bookService.deleteById(invalidBookId);
        });
        Assertions.assertTrue(exception.getMessage().contains("Can't delete book. Book with id "
                + invalidBookId + " doesn't exist"));

        verify(bookRepository, never()).deleteById(invalidBookId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException during save if some category IDs are missing in database")
    public void save_InValidRequest_ThrowsEntityNotFoundException() {
        Set<Long> categoryIds = Set.of(1L, 2L);
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setCategoryIds(categoryIds);

        Book book = new Book();

        when(bookMapper.toEntity(requestDto)).thenReturn(book);

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        List<Category> foundCategories = List.of(existingCategory);

        when(categoryRepository.findAllById(categoryIds)).thenReturn(foundCategories);

        EntityNotFoundException exception =
                Assertions.assertThrows(EntityNotFoundException.class,
                        () -> bookService.save(requestDto));
        Assertions.assertTrue(exception.getMessage()
                .contains("Could not find categories with ids"));

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should successfully map, save and return a BookDto when all data and categories are valid")
    public void save_ValidRequest_ReturnsBookDto() {
        Set<Long> categoryIds = Set.of(1L);
        CreateBookRequestDto requestDto = new CreateBookRequestDto();
        requestDto.setCategoryIds(categoryIds);

        Book book = new Book();
        Category category = new Category();
        category.setId(1L);
        List<Category> foundCategories = List.of(category);

        BookDto expectedDto = new BookDto();
        expectedDto.setTitle("Test Title");

        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        when(categoryRepository.findAllById(categoryIds)).thenReturn(foundCategories);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expectedDto);

        BookDto actualDto = bookService.save(requestDto);

        Assertions.assertNotNull(actualDto);
        Assertions.assertEquals(expectedDto, actualDto);

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should return a paginated list of BookDtos for any valid pageable request")
    public void findAll_ValidPageable_ReturnsAllBooks() {
        Book book = new Book();
        book.setTitle("Test title");

        BookDto bookDto = new BookDto();
        bookDto.setTitle("Test title");

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, 1);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        Page<BookDto> actualPage = bookService.findAll(pageable);
        List<BookDto> actualContent = actualPage.getContent();

        Assertions.assertEquals(1, actualContent.size());
        Assertions.assertEquals(bookDto.getTitle(), actualContent.get(0).getTitle());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return a page of books matching the specified search criteria")
    public void search_ValidParams_ReturnsPageOfBookDtos() {
        BookSearchParametersDto searchParams = new BookSearchParametersDto(
                new String[]{"Hyperion"},
                new String[]{"Dan Simmons"},
                new String[]{"978-83-7480-555-1"}
        );

        Pageable pageable = PageRequest.of(0, 10);

        Specification<Book> spec = (
                root, query, criteriaBuilder) -> null;

        Book book = new Book();
        book.setTitle("Hyperion");
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        BookDto bookDto = new BookDto();
        bookDto.setTitle("Hyperion");

        when(bookSpecificationBuilder.build(searchParams)).thenReturn(spec);
        when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        Page<BookDto> actualPage = bookService.search(searchParams, pageable);

        Assertions.assertNotNull(actualPage);
        Assertions.assertEquals(1, actualPage.getTotalElements());
        Assertions.assertEquals("Hyperion", actualPage.getContent().get(0).getTitle());

        verify(bookSpecificationBuilder).build(searchParams);
        verify(bookRepository).findAll(spec, pageable);
    }

    @Test
    @DisplayName("Should return an empty page when no books match the search parameters")
    public void search_NoMatches_ReturnsEmptyPage() {
        BookSearchParametersDto searchParametersDto = new  BookSearchParametersDto(
                new String[]{"Hyperion"},
                new String[]{"Dan Simmons"},
                new String[]{"978-83-7480-555-1"}
        );

        Pageable pageable = PageRequest.of(0, 10);
        Specification<Book> spec = (
                root, query, criteriaBuilder) -> null;

        Page<Book> bookPage = new PageImpl<>(List.of(), pageable, 0);

        when(bookSpecificationBuilder.build(searchParametersDto)).thenReturn(spec);
        when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);

        Page<BookDto> actualDto = bookService.search(searchParametersDto, pageable);

        Assertions.assertNotNull(bookPage);
        Assertions.assertTrue(actualDto.isEmpty());
        Assertions.assertEquals(0, bookPage.getTotalElements());

        verify(bookRepository).findAll(spec, pageable);
    }
}
