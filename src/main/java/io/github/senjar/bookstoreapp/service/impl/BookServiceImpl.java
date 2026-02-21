package io.github.senjar.bookstoreapp.service.impl;

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
import io.github.senjar.bookstoreapp.service.BookService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BookDto save(CreateBookRequestDto bookRequestDto) {
        Book book = bookMapper.toEntity(bookRequestDto);
        Set<Long> categoryIds = bookRequestDto.getCategoryIds();
        List<Category> categoryList = categoryRepository.findAllById(categoryIds);

        if (categoryIds.size() != categoryList.size()) {
            List<Long> foundIds = categoryList.stream()
                    .map(Category::getId)
                    .toList();

            List<Long> missingIds = categoryIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new EntityNotFoundException("Could not find categories with ids: " + missingIds);
        }

        book.setCategories(new HashSet<>(categoryList));
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDto(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        return bookMapper.toDto(bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id)));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete book. Book with id "
                    + id + " doesn't exist");
        }
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BookDto update(CreateBookRequestDto requestDto, Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id));

        bookMapper.updateBookFromDto(requestDto, book);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> search(BookSearchParametersDto searchParameters, Pageable pageable) {
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(searchParameters);

        return bookRepository.findAll(bookSpecification, pageable)
                .map(bookMapper::toDto);
    }
}
