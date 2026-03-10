package io.github.senjar.bookstoreapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.dto.order.OrderItemDto;
import io.github.senjar.bookstoreapp.dto.order.UpdateOrderStatusRequestDto;
import io.github.senjar.bookstoreapp.exception.BadRequestException;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.model.order.Order;
import io.github.senjar.bookstoreapp.model.order.Status;
import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.security.CustomUserDetailsService;
import io.github.senjar.bookstoreapp.security.JwtUtil;
import io.github.senjar.bookstoreapp.service.OrderService;

@WebMvcTest(
        value = OrderController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@DisplayName("Order Controller Tests")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should return a paginated list of orders for the authenticated user")
    void getAllOrderByUser_validRequest_returnsOrderDtoPage() throws Exception {
        User user = mockUser(1L);

        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        Page<OrderDto> orderDtoPage =
                new PageImpl<>(List.of(orderDto), PageRequest.of(0, 20), 1);

        when(orderService.getAllOrdersByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(orderDtoPage);

        mockMvc.perform(get("/api/orders")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should return all items belonging to a specific valid order")
    void getItemsFromOrder_validOrderId_returnsOrderItemDtoSet() throws Exception {
        User user = mockUser(1L);
        Long orderId = 10L;

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setId(orderId);

        when(orderService.getOrderItems(eq(1L), eq(orderId)))
                .thenReturn(Set.of(orderItemDto));

        mockMvc.perform(get("/api/orders/{orderId}/items", orderId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId));
    }

    @Test
    @DisplayName("Should return 404 Not Found when requesting items from a non-existing order")
    void getItemsFromOrder_invalidOrderId_throwsEntityNotFound() throws Exception {
        User user = mockUser(1L);
        Long invalidOrderId = 999L;

        when(orderService.getOrderItems(eq(1L), eq(invalidOrderId)))
                .thenThrow(new EntityNotFoundException(
                        "Order with id " + invalidOrderId + " not found"));

        mockMvc.perform(get("/api/orders/{orderId}/items", invalidOrderId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return detailed information about a specific item within an order")
    void getItemInfo_validRequest_returnsOrderItemDto() throws Exception {
        User user = mockUser(1L);
        Long orderId = 2L;
        Long itemId = 23L;

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setId(itemId);

        when(orderService.getItemInfo(eq(1L), eq(orderId), eq(itemId)))
                .thenReturn(orderItemDto);

        mockMvc.perform(get("/api/orders/{orderId}/items/{itemId}", orderId, itemId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
    }

    @Test
    @DisplayName("Should return 404 Not Found when requesting a specific item that does not exist")
    void getItemInfo_invalidItemId_throwsEntityNotFoundException() throws Exception {
        User user = mockUser(1L);
        Long orderId = 2L;
        Long invalidItemId = 999L;

        when(orderService.getItemInfo(eq(1L), eq(orderId), eq(invalidItemId)))
                .thenThrow(new EntityNotFoundException(
                        "Item with id " + invalidItemId + " not found"));

        mockMvc.perform(get("/api/orders/{orderId}/items/{invalidItemId}", orderId,
                        invalidItemId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 Not Found when requesting item info from a non-existing order")
    void getItemInfo_invalidOrderId_throwsEntityNotFoundException() throws Exception {
        User user = mockUser(1L);
        Long invalidOrderId = 999L;
        Long itemId = 23L;

        when(orderService.getItemInfo(eq(1L), eq(invalidOrderId), eq(itemId)))
                .thenThrow(new EntityNotFoundException(
                        "Order with id " + invalidOrderId + " not found"));

        mockMvc.perform(get("/api/orders/{orderId}/items/{itemId}",
                        invalidOrderId, itemId)
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should successfully place a new order and return order details")
    void placeOrder_validRequest_returnsOrderDto() throws Exception {
        User user = mockUser(1L);
        CreateOrderRequestDto requestDto = new CreateOrderRequestDto();
        requestDto.setShippingAddress("Random address 13");

        OrderDto orderDto = new OrderDto();
        orderDto.setShippingAddress(requestDto.getShippingAddress());

        when(orderService.placeOrder(anyLong(), any(CreateOrderRequestDto.class)))
                .thenReturn(orderDto);

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(jsonPath("$.shippingAddress")
                        .value("Random address 13"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when order placement fails due to invalid data")
    void placeOrder_invalidRequest_throwsBadRequestException() throws Exception {
        User user = mockUser(1L);
        CreateOrderRequestDto invalidRequest = new CreateOrderRequestDto();
        invalidRequest.setShippingAddress("");

        when(orderService.placeOrder(anyLong(), any(CreateOrderRequestDto.class)))
                .thenThrow(new BadRequestException("Shipping address incorrect"));

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .with(user(user))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should allow Admin to successfully update order status")
    void updateOrderStatus_validRequest_returnsOrderDto() throws Exception {
        User admin = mockAdmin(1L);
        Order order = new Order();
        Long orderId = 1L;
        order.setId(orderId);

        UpdateOrderStatusRequestDto updateOrderStatusRequestDto = new UpdateOrderStatusRequestDto();
        updateOrderStatusRequestDto.setStatus(Status.PENDING);

        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(Status.PENDING);

        when(orderService.updateOrderStatus(order.getId(), updateOrderStatusRequestDto))
                .thenReturn(orderDto);

        mockMvc.perform(patch("/api/orders/{id}", orderId)
                .with(user(admin))
                .with(csrf())
                .content(objectMapper.writeValueAsString(updateOrderStatusRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 Not Found when Admin tries to update status of a non-existing order")
    void updateOrderStatus_invalidOrderId_throwsEntityNotFoundException() throws Exception {
        User admin = mockAdmin(1L);
        Long invalidId = 999L;

        UpdateOrderStatusRequestDto updateOrderStatusRequestDto = new UpdateOrderStatusRequestDto();
        updateOrderStatusRequestDto.setStatus(Status.PENDING);

        when(orderService.updateOrderStatus(invalidId, updateOrderStatusRequestDto))
                .thenThrow(new EntityNotFoundException("Order with " + invalidId + " not found"));

        mockMvc.perform(patch("/api/orders/{id}", invalidId)
                .with(csrf())
                .with(user(admin))
                .content(objectMapper.writeValueAsString(updateOrderStatusRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Admin provides an invalid status update")
    void updateOrderStatus_invalidRequest_throwsBadRequestException() throws Exception {
        User admin = mockAdmin(1L);
        Long orderId = 1L;

        UpdateOrderStatusRequestDto updateOrderStatusRequestDto = new UpdateOrderStatusRequestDto();

        when(orderService.updateOrderStatus(orderId, updateOrderStatusRequestDto))
                .thenThrow(new BadRequestException("Status cannot be null"));

        mockMvc.perform(patch("/api/orders/{id}", orderId)
                .with(csrf())
                .with(user(admin))
                .content(objectMapper.writeValueAsString(updateOrderStatusRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private User mockUser(Long id) {
        Role userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);

        User user = new User();
        user.setId(id);
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRoles(Set.of(userRole));
        return user;
    }

    private User mockAdmin(Long id) {
        Role adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);

        User user = new User();
        user.setId(id);
        user.setEmail("admin@test.com");
        user.setPassword("password");
        user.setRoles(Set.of(adminRole));
        return user;
    }

}