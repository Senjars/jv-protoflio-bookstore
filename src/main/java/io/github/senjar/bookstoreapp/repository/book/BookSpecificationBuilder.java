package io.github.senjar.bookstoreapp.repository.book;

import io.github.senjar.bookstoreapp.dto.book.BookSearchParametersDto;
import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.repository.SpecificationBuilder;
import io.github.senjar.bookstoreapp.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements
        SpecificationBuilder<Book, BookSearchParametersDto> {

    private final SpecificationProviderManager<Book> specificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto bookSearchParameters) {

        Specification<Book> spec = Specification.where(null);
        if (bookSearchParameters.title() != null && bookSearchParameters.title().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("title")
                    .getSpecification(bookSearchParameters.title()));
        }
        if (bookSearchParameters.author() != null && bookSearchParameters.author().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("author")
                    .getSpecification(bookSearchParameters.author()));
        }
        if (bookSearchParameters.isbn() != null && bookSearchParameters.isbn().length > 0) {
            spec = spec.and(specificationProviderManager.getSpecificationProvider("isbn")
                    .getSpecification(bookSearchParameters.isbn()));
        }
        return spec;
    }
}
