package io.github.senjar.bookstoreapp.repository.book.spec;

import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class AuthorSpecificationProvider implements SpecificationProvider<Book> {

    @Override
    public String getKey() {
        return "author";
    }

    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder)
                -> root.get("author").in((Object[]) params);
    }
}
