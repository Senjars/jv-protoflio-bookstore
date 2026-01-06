package io.github.senjar.bookstoreapp.repository;

import io.github.senjar.bookstoreapp.model.Book;
import java.util.List;

public interface BookRepository {

    Book save(Book book);

    List<Book> findAll();

    Book findById(Long id);
}
