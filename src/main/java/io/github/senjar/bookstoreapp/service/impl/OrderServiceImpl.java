package io.github.senjar.bookstoreapp.service.impl;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.dto.order.OrderItemDto;
import io.github.senjar.bookstoreapp.dto.order.UpdateOrderStatusRequestDto;
import io.github.senjar.bookstoreapp.exception.AccessDeniedException;
import io.github.senjar.bookstoreapp.exception.BadRequestException;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.OrderItemMapper;
import io.github.senjar.bookstoreapp.mapper.OrderMapper;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;
import io.github.senjar.bookstoreapp.model.order.Order;
import io.github.senjar.bookstoreapp.model.order.OrderItem;
import io.github.senjar.bookstoreapp.model.order.Status;
import io.github.senjar.bookstoreapp.repository.order.OrderItemRepository;
import io.github.senjar.bookstoreapp.repository.order.OrderRepository;
import io.github.senjar.bookstoreapp.service.OrderService;
import io.github.senjar.bookstoreapp.service.ShoppingCartService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemRepository itemRepository;
    private final OrderItemMapper orderItemMapper;
    private final ShoppingCartService shoppingCartService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrdersByUserId(Long userId, Pageable pageable) {

        return orderRepository.findAllByUserId(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<OrderItemDto> getOrderItems(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your orders");
        }

        return itemRepository.findAllByOrderId(orderId).stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemDto getItemInfo(Long userId, Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can access your own orders");
        }

        OrderItem orderItem = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Item with id: " + itemId + " not found"));

        if (!orderItem.getOrder().getId().equals(orderId)) {
            throw new AccessDeniedException("Item with id " + itemId + " is not part of order "
                    + orderId);
        }

        return orderItemMapper.toDto(orderItem);
    }

    @Override
    @Transactional
    public OrderDto placeOrder(Long userId, CreateOrderRequestDto requestDto) {
        ShoppingCart shoppingCart = shoppingCartService.getOrCreateCart(userId);

        if (shoppingCart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart!");
        }

        Order order = orderMapper.toEntity(requestDto);
        order.setUser(shoppingCart.getUser());
        order.setStatus(Status.PENDING);
        order.setOrderDate(LocalDateTime.now());

        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setBook(cartItem.getBook());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getBook().getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                }).collect(Collectors.toSet());

        order.setOrderItems(orderItems);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem o : orderItems) {
            BigDecimal itemTotal = o.getPrice().multiply(BigDecimal.valueOf(o.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        order.setTotal(totalPrice);
        Order savedOrder = orderRepository.save(order);

        shoppingCart.getCartItems().clear();

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId,
                                      UpdateOrderStatusRequestDto requestDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Order with id: " + orderId + " not found"));

        orderMapper.updateOrderFromDto(requestDto, order);
        return orderMapper.toDto(orderRepository.save(order));
    }

}
