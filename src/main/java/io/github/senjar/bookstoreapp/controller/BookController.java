package io.github.senjar.bookstoreapp.controller;

import io.github.senjar.bookstoreapp.dto.book.BookDto;
import io.github.senjar.bookstoreapp.dto.book.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.dto.book.CreateBookRequestDto;
import io.github.senjar.bookstoreapp.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "books", description = "Operations for managing books")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get all books",
            description = "Returns a list of all books",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Books found"),
            }
    )
    public Page<BookDto> getAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return bookService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get a book by ID",
            description = "Returns a single book with all details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Book found"),
                    @ApiResponse(responseCode = "404", description = "Book not found")
            }
    )
    public BookDto getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a book",
            description = "Creates a single book",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Book created"),
            }
    )
    public BookDto createBook(@Valid @RequestBody CreateBookRequestDto bookRequestDto) {
        return bookService.save(bookRequestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a book by ID",
            description = "Deletes a single book",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Book has been deleted"),
                    @ApiResponse(responseCode = "404", description = "Could not delete the book")
            }
    )
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a book by ID",
            description = "Updates details of a book",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Book updated"),
                    @ApiResponse(responseCode = "404", description = "Failed to update the book")
            }
    )
    public BookDto updateBook(@Valid @RequestBody CreateBookRequestDto requestDto,
                              @PathVariable Long id) {
        return bookService.update(requestDto, id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Search for books by title, author or ISBN",
            description = "Finds books by title, author or ISBN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Books found"),
            }
    )
    public Page<BookDto> search(@ParameterObject BookSearchParametersDto bookSearchParameters,
                                @ParameterObject
                                @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return bookService.search(bookSearchParameters, pageable);
    }
}
