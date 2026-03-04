package io.github.senjar.bookstoreapp;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication
public class BookstoreAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreAppApplication.class, args);
    }
}

