package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.model.Book;
import java.util.List;

public interface BookService {

    Book save(Book book);

    List<Book> findAll();
}
