package io.github.senjar.bookstoreapp.repository;

import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Should find all books associated with a specific category ID")
    @Sql(scripts = "classpath:database/books/insert-book-to-books.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/books/remove-book-from-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void findAllByCategoriesId_ValidCategoryId_ReturnsBooksWithAdequateCategoryId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> actual = bookRepository.findAllByCategoriesId(1L, pageable);

        Assertions.assertEquals(1L, actual.getContent().size());
    }

}
