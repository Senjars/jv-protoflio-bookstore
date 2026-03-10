package io.github.senjar.bookstoreapp.repository;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import io.github.senjar.bookstoreapp.config.BaseContainerTest;
import io.github.senjar.bookstoreapp.model.order.OrderItem;
import io.github.senjar.bookstoreapp.repository.order.OrderItemRepository;

@DataJpaTest
@DisplayName("Order Item Repository Integration Tests")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderItemRepositoryTest extends BaseContainerTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("Should return set of order items for valid order ID")
    @Sql(scripts = "classpath:database/orderitems/insert-order-item.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/orderitems/remove-order-item.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByOrderId_validId_returnsOrderItemSet() {
        Set<OrderItem> actual = orderItemRepository.findAllByOrderId(997L);

        Assertions.assertEquals(1, actual.size());
    }

    @Test
    @DisplayName("Should return empty set when order ID has no items or does not exist")
    void findAllByOrderId_noOrderItems_returnsOrderItemSet() {
        Set<OrderItem> actual = orderItemRepository.findAllByOrderId(999L);

        Assertions.assertNotNull(actual);
        Assertions.assertTrue(actual.isEmpty());
    }
}
