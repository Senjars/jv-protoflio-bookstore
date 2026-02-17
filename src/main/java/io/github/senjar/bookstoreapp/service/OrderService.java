package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.dto.order.OrderItemDto;
import io.github.senjar.bookstoreapp.dto.order.UpdateOrderStatusRequestDto;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderDto> getAllOrdersByUserId(Long userId, Pageable pageable);

    Set<OrderItemDto> getOrderItems(Long userId, Long orderId);

    OrderItemDto getItemInfo(Long userId,Long itemId);

    OrderDto placeOrder(Long userId, CreateOrderRequestDto requestDto);

    OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequestDto requestDto);
}
