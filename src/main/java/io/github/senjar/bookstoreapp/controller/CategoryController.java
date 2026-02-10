package io.github.senjar.bookstoreapp.controller;

import io.github.senjar.bookstoreapp.dto.book.BookDtoWithoutCategoryIds;
import io.github.senjar.bookstoreapp.dto.category.CategoryDto;
import io.github.senjar.bookstoreapp.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a category",
            description = "Creates a single category",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category created")
            }
    )
    public CategoryDto createCategory(@RequestBody @Valid CategoryDto categoryDto) {
        return categoryService.save(categoryDto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get all categories",
            description = "Returns a list of all categories",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories found")
            }
    )
    public Page<CategoryDto> getAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get a category by ID",
            description = "Returns a single category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category found"),
                    @ApiResponse(responseCode = "404", description = "Category not found")
            }
    )
    public CategoryDto getCategoryById(
            @PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a category by ID",
            description = "Updates a category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated"),
                    @ApiResponse(responseCode = "404",
                            description = "Failed to update the category")
            }
    )
    public CategoryDto updateCategory(@PathVariable Long id,
                                      @RequestBody @Valid CategoryDto categoryDto) {
        return categoryService.update(id, categoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a category by ID",
            description = "Deletes a category by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category has been deleted"),
                    @ApiResponse(responseCode = "404",
                            description = "Failed to delete the category")
            }
    )
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
    }

    @GetMapping("/{id}/books")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Return a list of books by category ID",
            description = "Returns a list of books matching the category ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Books found")
            }
    )
    public Page<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @PathVariable Long id, @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return categoryService.findBooksByCategoriesId(id, pageable);
    }
}
