package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.book.BookDto;
import io.github.senjar.bookstoreapp.dto.book.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.dto.book.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto save(CreateBookRequestDto bookRequestDto);

    Page<BookDto> findAll(Pageable pageable);

    BookDto getBookById(Long id);

    void deleteById(Long id);

    BookDto update(CreateBookRequestDto requestDto, Long id);

    Page<BookDto> search(BookSearchParametersDto searchParameters, Pageable pageable);
}
