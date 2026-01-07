package io.github.senjar.bookstoreapp.repository;

import io.github.senjar.bookstoreapp.repository.book.BookSearchParameters;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookSearchParameters bookSearchParameters);
}
