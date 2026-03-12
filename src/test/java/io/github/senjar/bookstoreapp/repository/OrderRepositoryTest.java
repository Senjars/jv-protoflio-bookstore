package io.github.senjar.bookstoreapp.repository;

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

import io.github.senjar.bookstoreapp.config.BaseContainerTest;
import io.github.senjar.bookstoreapp.model.order.Order;
import io.github.senjar.bookstoreapp.repository.order.OrderRepository;

@DataJpaTest
@DisplayName("Order Repository Integration Tests")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest extends BaseContainerTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Should return page with orders when user has existing orders")
    @Sql(scripts = "classpath:database/orders/insert-order.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/orders/remove-order.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByUserId_validUserId_returnsPageWithOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> actual = orderRepository.findAllByUserId(998L, pageable);

        Assertions.assertEquals(1L, actual.getTotalElements());
    }

    @Test
    @DisplayName("Should return an empty page when searching for a user with no orders")
    void findAllByUserId_noOrders_returnsPageWithNoOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> actual = orderRepository.findAllByUserId(999L, pageable);

        Assertions.assertEquals(0, actual.getTotalElements());
    }
}
