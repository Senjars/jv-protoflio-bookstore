package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.BookDto;
import io.github.senjar.bookstoreapp.dto.CreateBookRequestDto;
import io.github.senjar.bookstoreapp.repository.book.BookSearchParameters;
import java.util.List;

public interface BookService {

    BookDto save(CreateBookRequestDto bookRequestDto);

    List<BookDto> findAll();

    BookDto getBookById(Long id);

    void deleteById(Long id);

    BookDto update(CreateBookRequestDto requestDto, Long id);

    List<BookDto> search(BookSearchParameters searchParameters);
}
