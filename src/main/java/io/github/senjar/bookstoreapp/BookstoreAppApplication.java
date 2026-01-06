package io.github.senjar.bookstoreapp;

import io.github.senjar.bookstoreapp.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication
public class BookstoreAppApplication {

    private final BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(BookstoreAppApplication.class, args);
    }
}

