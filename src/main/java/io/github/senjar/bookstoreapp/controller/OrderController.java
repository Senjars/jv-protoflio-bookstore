package io.github.senjar.bookstoreapp.controller;

import io.github.senjar.bookstoreapp.dto.order.CreateOrderRequestDto;
import io.github.senjar.bookstoreapp.dto.order.OrderDto;
import io.github.senjar.bookstoreapp.dto.order.OrderItemDto;
import io.github.senjar.bookstoreapp.dto.order.UpdateOrderStatusRequestDto;
import io.github.senjar.bookstoreapp.model.User;
import io.github.senjar.bookstoreapp.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "orders", description = "Operations for managing orders")
@RequestMapping("/api/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public Page<OrderDto> getAllOrdersByUser(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        User user = (User) authentication.getPrincipal();
        return orderService.getAllOrdersByUserId(user.getId(), pageable);
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasRole('USER')")
    public Set<OrderItemDto> getItemsFromOrder(Authentication authentication,
                                               @PathVariable Long orderId) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderItems(user.getId(), orderId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get information about an ordered item",
            description = "Provides information about an ordered item",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successfully retrieved order item information"),
                    @ApiResponse(responseCode = "404",
                            description = "Can't get information about the product")
            }
    )
    public OrderItemDto getItemInfo(Authentication authentication,
                                    @PathVariable Long orderId,
                                    @PathVariable Long itemId) {
        User user = (User) authentication.getPrincipal();
        return orderService.getItemInfo(user.getId(), orderId, itemId);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Make an order",
            description = "Makes an order",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order successfully placed"),
                    @ApiResponse(responseCode = "400", description = "Invalid order data provided")
            }
    )
    public OrderDto placeOrder(Authentication authentication,
                               @Valid @RequestBody CreateOrderRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return orderService.placeOrder(user.getId(), requestDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update an order status",
            description = "Updates the order status of a single order",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Order status updated"),
                    @ApiResponse(responseCode = "404",
                            description = "Failed to update the order status")
            }
    )
    public OrderDto updateOrderStatus(@PathVariable(name = "id") Long orderId,
                                      @Valid @RequestBody UpdateOrderStatusRequestDto requestDto) {
        return orderService.updateOrderStatus(orderId, requestDto);
    }
}
