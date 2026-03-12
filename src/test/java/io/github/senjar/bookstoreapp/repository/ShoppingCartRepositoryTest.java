package io.github.senjar.bookstoreapp.repository;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import io.github.senjar.bookstoreapp.config.BaseContainerTest;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;
import io.github.senjar.bookstoreapp.repository.shoppingcart.ShoppingCartRepository;

@DataJpaTest
@DisplayName("Shopping Cart Repository Integration Tests")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ShoppingCartRepositoryTest extends BaseContainerTest {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    @DisplayName("Should return shopping cart with items when valid user ID is provided")
    @Sql(scripts = "classpath:database/shoppingcart/insert-shopping-cart.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/shoppingcart/remove-shopping-cart.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserId_validId_returnsShoppingCart() {
        Optional<ShoppingCart> actual = shoppingCartRepository.findByUserId(999L);

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(999L, actual.get().getUser().getId());
    }

    @Test
    @DisplayName("Should return empty optional when user has no shopping cart")
    void findByUserId_noCart_returnsEmptyOptional() {
        Optional<ShoppingCart> actual = shoppingCartRepository.findByUserId(999L);

        Assertions.assertTrue(actual.isEmpty());
    }
}