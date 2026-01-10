package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.BookDto;
import io.github.senjar.bookstoreapp.dto.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.dto.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto save(CreateBookRequestDto bookRequestDto);

    List<BookDto> findAll(Pageable pageable);

    BookDto getBookById(Long id);

    void deleteById(Long id);

    BookDto update(CreateBookRequestDto requestDto, Long id);

    List<BookDto> search(BookSearchParametersDto searchParameters);
}
