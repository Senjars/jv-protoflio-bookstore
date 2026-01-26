package io.github.senjar.bookstoreapp.mapper;

import io.github.senjar.bookstoreapp.dto.book.BookDto;
import io.github.senjar.bookstoreapp.dto.book.CreateBookRequestDto;
import io.github.senjar.bookstoreapp.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookDto toDto(Book book);

    Book toModel(CreateBookRequestDto createBookRequestDto);

    @Mapping(target = "id", ignore = true)
    void updateBookFromDto(CreateBookRequestDto createBookRequestDto, @MappingTarget Book book);
}
