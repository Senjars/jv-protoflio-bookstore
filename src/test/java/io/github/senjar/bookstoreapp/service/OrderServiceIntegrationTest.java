package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.exception.BadRequestException;
import io.github.senjar.bookstoreapp.model.order.Status;
import io.github.senjar.bookstoreapp.config.BaseContainerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest extends BaseContainerTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Place Order: Should successfully place order from populated cart")
    @Sql(scripts = {
            "classpath:database/shoppingcart/insert-shopping-cart.sql",
            "classpath:database/books/insert-book-to-books.sql",
            "classpath:database/shoppingcart/insert-cart-items.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/shoppingcart/remove-shopping-cart.sql",
            "classpath:database/books/remove-book-from-books.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void placeOrder_validCart_returnsOrderDto() {
        Long userId = 999L;
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setShippingAddress("TestAddress 123");

        OrderDto createdOrder = orderService.placeOrder(userId, requestDto);

        Assertions.assertNotNull(createdOrder.getId());
        Assertions.assertEquals(Status.PENDING, createdOrder.getStatus());
        Assertions.assertEquals("TestAddress 123", createdOrder.getShippingAddress());
        Assertions.assertEquals(new BigDecimal("45.00"), createdOrder.getTotal());
        Assertions.assertFalse(createdOrder.getOrderItems().isEmpty());
    }

    @Test
    @DisplayName("Place Order: Should throw exception when cart is empty")
    @Sql(scripts = "classpath:database/shoppingcart/insert-shopping-cart.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/shoppingcart/remove-shopping-cart.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void placeOrder_emptyCart_throwsBadRequestException() {
        Long userId = 999L;
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setShippingAddress("TestAddress 123");

        Assertions.assertThrows(BadRequestException.class, () ->
                orderService.placeOrder(userId, requestDto)
        );
    }
}