package io.github.senjar.bookstoreapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.senjar.bookstoreapp.dto.book.BookDto;
import io.github.senjar.bookstoreapp.dto.book.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.dto.book.CreateBookRequestDto;
import io.github.senjar.bookstoreapp.exception.BadRequestException;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.security.CustomUserDetailsService;
import io.github.senjar.bookstoreapp.security.JwtUtil;
import io.github.senjar.bookstoreapp.service.BookService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = BookController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@DisplayName("Book Controller Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should successfully create a new book when request is valid")
    void create_validRequest_returnsBookDto() throws Exception {
        CreateBookRequestDto requestDto = createValidRequestDto();
        BookDto expected = createBookDto();

        when(bookService.save(any(CreateBookRequestDto.class))).thenReturn(expected);

        mockMvc.perform(
                        post("/api/books")
                                .with(csrf())
                                .content(objectMapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"))
                .andExpect(jsonPath("$.isbn").value("978-01-34685-99-1"))
                .andExpect(jsonPath("$.price").value(150.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 Bad Request when book creation data is invalid")
    void create_invalidRequest_throwsBadRequestException() throws Exception {
        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto()
                .setTitle("");

        mockMvc.perform(post("/api/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookRequestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update book details successfully when ID and data are valid")
    void update_validRequest_returnsBookDto() throws Exception {
        Long id = 1L;
        CreateBookRequestDto requestDto = createValidRequestDto();
        BookDto expected = createBookDto();

        when(bookService.update(requestDto, id)).thenReturn(expected);

        mockMvc.perform(put("/api/books/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Joshua Bloch"))
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 Bad Request when updating with incomplete data")
    void update_invalidRequest_throwsBadRequestException() throws Exception {
        Long id = 1L;
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch");

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/api/books/{id}", id)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when attempting to update a non-existing book")
    void update_invalidId_throwsEntityNotFoundException() throws Exception {
        Long invalidId = 999L;
        CreateBookRequestDto requestDto = createValidRequestDto();

        when(bookService.update(requestDto, invalidId)).thenThrow(
                new EntityNotFoundException("Book with id: " + invalidId + " not found"));

        mockMvc.perform(put("/api/books/{invalidId}", invalidId)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return a paginated list of all books for authenticated users")
    void getAll_validRequest_returnsPageOfBooks() throws Exception {
        BookDto bookDto = createBookDto();

        List<BookDto> books = List.of(bookDto);
        Page<BookDto> bookDtoPage = new PageImpl<>(
                books, PageRequest.of(0, 10), 1);

        when(bookService.findAll(any(Pageable.class))).thenReturn(bookDtoPage);

        mockMvc.perform(get("/api/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Effective Java"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return book details when a valid ID is provided")
    void getById_validId_returnsBookDto() throws Exception {
        Long id = 1L;
        BookDto expected = createBookDto();

        when(bookService.getBookById(id)).thenReturn(expected);

        mockMvc.perform(get("/api/books/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 404 Not Found when searching for a book with a non-existing ID")
    void getById_invalidId_throwsEntityNotFoundException() throws Exception {
        Long invalidId = 999L;
        when(bookService.getBookById(invalidId)).thenThrow(
                new EntityNotFoundException("Book with id: "  + invalidId + " not found"));

        mockMvc.perform(get("/api/books/{invalidId}", invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should successfully delete a book when a valid ID is provided")
    void delete_validId_returnsNoContent() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/books/{id}", id)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when trying to delete a book that doesn't exist")
    void delete_invalidId_throwsEntityNotFoundException() throws Exception {
        Long invalidId = 999L;

        doThrow(new EntityNotFoundException("Book with id: " + invalidId + " not found"))
                .when(bookService).deleteById(invalidId);

        mockMvc.perform(delete("/api/books/{invalidId}", invalidId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return a list of books matching the search criteria")
    void search_validParameters_returnsPageOfBooks() throws Exception {
        BookDto bookDto = new BookDto()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch");
        List<BookDto> books = List.of(bookDto);
        Page<BookDto> bookDtoPage = new PageImpl<>(
                books, PageRequest.of(0, 5), 1);

        when(bookService.search(any(BookSearchParametersDto.class), any(Pageable.class)))
                .thenReturn(bookDtoPage);

        mockMvc.perform(get("/api/books/search")
                    .param("author", "Bloch")
                    .param("page", "0")
                    .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].author").value("Joshua Bloch"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(bookService).search(any(BookSearchParametersDto.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 400 Bad Request when search parameters are incorrect")
    void search_invalidParameters_throwsBadRequestException() throws Exception {
        doThrow(new BadRequestException("Wrong search parameters"))
                .when(bookService).search(any(), any());

        mockMvc.perform(get("/api/books/search")
                        .param("titles", "invalidTitle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private CreateBookRequestDto createValidRequestDto() {
        return new CreateBookRequestDto()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch")
                .setIsbn("978-01-34685-99-1")
                .setPrice(new BigDecimal("150.00"));
    }

    private BookDto createBookDto() {
        return new BookDto()
                .setId(1L)
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch")
                .setIsbn("978-01-34685-99-1")
                .setPrice(new BigDecimal("150.00"));
    }
}