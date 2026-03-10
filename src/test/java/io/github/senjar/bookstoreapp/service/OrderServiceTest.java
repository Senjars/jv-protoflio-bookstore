package io.github.senjar.bookstoreapp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.dto.order.OrderItemDto;
import io.github.senjar.bookstoreapp.dto.order.UpdateOrderStatusRequestDto;
import io.github.senjar.bookstoreapp.exception.AccessDeniedException;
import io.github.senjar.bookstoreapp.exception.BadRequestException;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.OrderItemMapper;
import io.github.senjar.bookstoreapp.mapper.OrderMapper;
import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.model.cart.CartItem;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;
import io.github.senjar.bookstoreapp.model.order.Order;
import io.github.senjar.bookstoreapp.model.order.OrderItem;
import io.github.senjar.bookstoreapp.model.order.Status;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.repository.order.OrderItemRepository;
import io.github.senjar.bookstoreapp.repository.order.OrderRepository;
import io.github.senjar.bookstoreapp.service.impl.OrderServiceImpl;
import io.github.senjar.bookstoreapp.service.impl.ShoppingCartServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Business Logic Test")
public class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Test
    @DisplayName("Should successfully retrieve a paginated list of orders for a specific user")
    void getAllOrdersByUserId_validId_returnsPageOfOrderDtos() {
        Long userId = 1L;
        Long orderId = 1L;
        User user = CreateUser(userId);

        Pageable pageable = PageRequest.of(0,10);
        OrderDto expectedDto = CreateOrderDto(user.getId(), userId);
        Order order = CreateOrder(userId, orderId);
        Page<Order> expected = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAllByUserId(userId, pageable)).thenReturn(expected);
        when(orderMapper.toDto(order)).thenReturn(expectedDto);

        Page<OrderDto> actualPage = orderService.getAllOrdersByUserId(userId, pageable);

        Assertions.assertEquals(expected.getTotalElements(), actualPage.getTotalElements());
        Assertions.assertEquals(expectedDto.getUserId(),
                actualPage.getContent().get(0).getUserId());

        verify(orderRepository).findAllByUserId(userId, pageable);
        verify(orderMapper).toDto(order);
    }


    @Test
    @DisplayName("Should return a set of items for a valid order when requested by the owner")
    void getOrderItems_validRequest_returnsOrderItemDtoSet() {
        Long userId = 1L;
        Long orderId = 1L;

        User user = CreateUser(userId);
        OrderDto orderDto = CreateOrderDto(userId, orderId);
        Order order = CreateOrder(userId, orderId);

        OrderItem orderItem = new OrderItem();
        OrderItemDto orderItemDto = new OrderItemDto();
        Set<OrderItem> orderItemList = Set.of(orderItem);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(orderItemList);
        when(orderItemMapper.toDto(orderItem)).thenReturn(orderItemDto);

        Set<OrderItemDto> actual = orderService.getOrderItems(userId, orderId);

        Assertions.assertEquals(orderItemList.size(), actual.size());
        Assertions.assertTrue(actual.contains(orderItemDto));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when requesting items for a non-existing order")
    void getOrderItems_invalidOrderId_throwsEntityNotFound() {
        Long userId = 1L;
        Long invalidOrderId = 999L;

        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
           orderService.getOrderItems(userId, invalidOrderId);
        });
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when a user tries to access items of someone else's order")
    void getOrderItems_userIsNotOwner_throwsAccessDeniedException() {
        Long currentUserId = 1L;
        Long realOwnerId = 13L;
        Long orderId = 5L;
        User user = CreateUser(realOwnerId);
        Order order = CreateOrder(realOwnerId, orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            orderService.getOrderItems(currentUserId, orderId);
        });

        verify(orderRepository, never()).findAllByUserId(any(), any());
    }

    @Test
    @DisplayName("Should return specific item details if the order belongs to the authenticated user")
    void getItemInfo_validRequest_returnsOrderItemDto() {
        Long userId = 1L;
        Long orderId = 13L;
        Long itemId = 23L;
        User user = CreateUser(userId);
        Order order = CreateOrder(userId, orderId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setOrder(order);

        OrderItemDto expectedDto = new OrderItemDto();
        expectedDto.setId(itemId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toDto(orderItem)).thenReturn(expectedDto);

        OrderItemDto actual = orderService.getItemInfo(userId, orderId, itemId);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedDto.getId(), actual.getId());

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when requesting a specific item that does not exist in order")
    void getItemInfo_invalidItemId_throwsEntityNotFoundException() {
        Long orderId = 13L;
        Long userId = 1L;
        Long invalidItemId = 999L;
        User user = CreateUser(userId);
        Order order = CreateOrder(userId, orderId);
        order.setUser(user);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            orderService.getItemInfo(userId, orderId, invalidItemId);
        });
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when requesting item info for a non-existing order ID")
    void getItemInfo_invalidOrderId_throwsEntityNotFoundException() {
        Long invalidOderId = 999L;
        Long userId = 1L;
        Long itemId = 23L;

        when(orderRepository.findById(invalidOderId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            orderService.getItemInfo(userId, invalidOderId, itemId);
        });
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when trying to view item details of an order owned by another user")
    void getItemInfo_userIsNotOwner_throwsAccessDeniedException() {
        Long currentUserId = 1L;
        Long realUserId = 23L;
        Long itemId = 25L;
        Long orderId = 13L;

        Order order = CreateOrder(realUserId, orderId);
        OrderItem item = new OrderItem();
        item.setId(itemId);
        item.setOrder(order);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Assertions.assertThrows(AccessDeniedException.class, () -> {
            orderService.getItemInfo(currentUserId, orderId, itemId);
        });

        verify(orderRepository, times(1)).findById(orderId);

    }

    @Test
    @DisplayName("Should successfully convert shopping cart to order and save it in the database")
    void placeOrder_validRequest_returnsOrderDto() {
        Long userId = 1L;
        Long orderId = 13L;
        CreateOrderRequestDto orderRequestDto = new CreateOrderRequestDto();
        orderRequestDto.setShippingAddress("Random ShippingAddress 123");
        Book book = new Book();
        book.setTitle("Hyperion");
        book.setPrice(BigDecimal.valueOf(10));

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(1);
        Set<CartItem> cartItemSet = new HashSet<>();
        cartItemSet.add(cartItem);
        ShoppingCart shoppingCart = new ShoppingCart();

        shoppingCart.setCartItems(cartItemSet);
        User user = CreateUser(userId);
        Order order = CreateOrder(userId, orderId);
        order.setUser(user);
        order.setTotal(BigDecimal.TEN);

        OrderDto orderDto = CreateOrderDto(userId, orderId);
        orderDto.setShippingAddress(orderRequestDto.getShippingAddress());

        shoppingCart.setUser(user);

        when(shoppingCartService.getOrCreateCart(userId)).thenReturn(shoppingCart);
        when(orderMapper.toEntity(orderRequestDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        OrderDto actual = orderService.placeOrder(userId, orderRequestDto);

        Assertions.assertEquals(orderDto.getShippingAddress(), actual.getShippingAddress());
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to place an order with an empty shopping cart")
    void placeOrder_invalidRequest_throwsBadRequestException() {
        Long userId = 1L;
        ShoppingCart emptyCart = new ShoppingCart();
        emptyCart.setCartItems(new HashSet<>());
        CreateOrderRequestDto orderRequestDto = new CreateOrderRequestDto();

        when(shoppingCartService.getOrCreateCart(userId)).thenReturn(emptyCart);

        Assertions.assertThrows(BadRequestException.class, () -> {
            orderService.placeOrder(userId, orderRequestDto);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should allow updating order status and return the updated order DTO")
    void updateOrderStatus_validRequest_ReturnsOrderDto() {
        Long orderId = 1L;
        Order order = new Order();
        UpdateOrderStatusRequestDto statusDto = new UpdateOrderStatusRequestDto();
        statusDto.setStatus(Status.PENDING);
        OrderDto expectedDto = new OrderDto();
        expectedDto.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(expectedDto);
        doNothing().when(orderMapper).updateOrderFromDto(statusDto, order);

        OrderDto actual = orderService.updateOrderStatus(orderId, statusDto);

        Assertions.assertEquals(expectedDto.getId(), actual.getId());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when trying to update status of a non-existing order")
    void updateOrderStatus_invalidId_throwsEntityNotFoundException() {
        Long invalidId = 999L;
        Order order = new Order();
        order.setId(invalidId);
        UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto();

        when(orderRepository.findById(invalidId)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
           orderService.updateOrderStatus(invalidId, requestDto);
        });

        verify(orderRepository, times(1)).findById(invalidId);
    }

    private Order CreateOrder(Long userId, Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        User user = new User();
        user.setId(userId);
        order.setUser(user);
        return order;
    }

    private OrderDto CreateOrderDto(Long userId, Long orderId) {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setId(orderId);
        return orderDto;
    }

    private User CreateUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
