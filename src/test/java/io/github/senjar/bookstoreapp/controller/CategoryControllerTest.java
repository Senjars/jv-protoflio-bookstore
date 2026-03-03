package io.github.senjar.bookstoreapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.senjar.bookstoreapp.dto.book.BookDtoWithoutCategoryIds;
import io.github.senjar.bookstoreapp.dto.book.CategoryDto;
import io.github.senjar.bookstoreapp.dto.book.CategoryRequestDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.security.CustomUserDetailsService;
import io.github.senjar.bookstoreapp.security.JwtUtil;
import io.github.senjar.bookstoreapp.service.CategoryService;
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
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
        value = CategoryController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@DisplayName("Category Controller Integration Tests")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should successfully create a new category when admin provides valid data")
    void createCategory_ValidRequest_CreatesNewCategory() throws Exception {
        CategoryRequestDto categoryDto = new CategoryRequestDto()
                .setName("Horror")
                .setDescription("Books with scary stories");

        CategoryDto expected = new CategoryDto();
        expected.setName(categoryDto.getName());
        expected.setDescription(categoryDto.getDescription());

        when(categoryService.save(any(CategoryRequestDto.class))).thenReturn(expected);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Horror"))
                .andExpect(jsonPath("$.description")
                        .value("Books with scary stories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 Bad Request when category creation data is invalid")
    void createCategory_InvalidRequest_ThrowsBadRequestException() throws Exception {
        CategoryRequestDto categoryDto = new CategoryRequestDto();

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USER" , "ADMIN"})
    @DisplayName("Should return a paginated list of all categories for authorized users")
    void getAll_ValidRequest_ReturnsAllCategories() throws Exception {
        CategoryDto categoryDto = new CategoryDto()
                .setName("Horror");
        List<CategoryDto> categories = List.of(categoryDto);
        Page<CategoryDto> categoryDtoPage = new PageImpl<>(
                categories, PageRequest.of(0,10), 1);

        when(categoryService.findAll(any(Pageable.class))).thenReturn(categoryDtoPage);

        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Horror"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("Should return category details when searching by a valid ID")
    void getCategoryById_ValidId_ReturnsCategory() throws Exception {
        Long id = 1L;
        CategoryDto categoryDto = new CategoryDto()
                .setId(id)
                .setName("Horror");

        when(categoryService.getById(id)).thenReturn(categoryDto);

        mockMvc.perform(get("/api/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Horror"));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("Should return 404 Not Found when category ID does not exist")
    void getCategoryById_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = -10L;

        when(categoryService.getById(invalidId))
                .thenThrow(new EntityNotFoundException(
                        "Category with id: " + invalidId + " not found"));

        mockMvc.perform(get("/api/categories/{invalidId}", invalidId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should successfully update category details when admin provides valid data")
    void updateCategory_ValidRequest_UpdatesCategory() throws Exception {
        Long id = 1L;
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(id);
        categoryDto.setName("Comedy");

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto()
                .setName("Comedy");

        when(categoryService.update(eq(id), any(CategoryRequestDto.class))).thenReturn(categoryDto);

        mockMvc.perform(put("/api/categories/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Comedy"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when trying to update a non-existing category")
    void updateCategory_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = -10L;

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto()
                .setName("Horror");

        when(categoryService.update(eq(invalidId), any(CategoryRequestDto.class))).thenThrow(
                new EntityNotFoundException("Category with id " + invalidId + " not found"));

        mockMvc.perform(put("/api/categories/{invalidId}", invalidId)
                .with(csrf())
                .content(objectMapper.writeValueAsString(categoryRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should successfully delete category when a valid ID is provided")
    void deleteCategory_ValidId_DeletesCategory() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/categories/{id}", id)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteById(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 Not Found when admin tries to delete a non-existing category")
    void deleteCategory_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = -10L;

        doThrow(new EntityNotFoundException("Category with id: " + invalidId + " not found"))
                .when(categoryService).deleteById(invalidId);

        mockMvc.perform(delete("/api/categories/{invalidId}", invalidId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("Should return a list of books belonging to a specific category")
    void getBooksByCategoryId_ValidId_ReturnsBookList() throws Exception {
        Long id = 1L;
        BookDtoWithoutCategoryIds bookDto = new BookDtoWithoutCategoryIds();

        List<BookDtoWithoutCategoryIds> books = List.of(bookDto);
        Page<BookDtoWithoutCategoryIds> bookDtoPage =
                new PageImpl<>(books, PageRequest.of(0, 10),1);

        when(categoryService.findBooksByCategoriesId(eq(id), any(Pageable.class)))
                .thenReturn(bookDtoPage);

        mockMvc.perform(get("/api/categories/{id}/books", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("Should return 404 Not Found when fetching books for a non-existing category")
    void getBooksByCategoryId_InvalidId_ThrowsEntityNotFoundException() throws Exception {
        Long invalidId = -10L;

        when(categoryService.findBooksByCategoriesId(eq(invalidId), any(Pageable.class)))
                .thenThrow(new EntityNotFoundException(
                        "Category with id: " + invalidId + " not found"));

        mockMvc.perform(get("/api/categories/{invalidId}/books", invalidId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
