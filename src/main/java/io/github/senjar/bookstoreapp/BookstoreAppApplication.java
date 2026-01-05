package io.github.senjar.bookstoreapp;

import io.github.senjar.bookstoreapp.model.Book;
import io.github.senjar.bookstoreapp.service.BookService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@SpringBootApplication
public class BookstoreAppApplication {

    private final BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(BookstoreAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book book = new Book();
            book.setTitle("Hyperion");
            book.setAuthor("Dan Simmons");
            book.setIsbn("ISBN");
            book.setPrice(BigDecimal.valueOf(25));
            book.setDescription("Book Description");
            book.setCoverImage("cover.jpg");

            bookService.save(book);
            System.out.println(bookService.findAll());
        };
    }
}

