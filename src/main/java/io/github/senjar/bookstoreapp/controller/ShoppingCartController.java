package io.github.senjar.bookstoreapp.controller;

import io.github.senjar.bookstoreapp.dto.shoppingcart.ItemRequestDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.UpdateQuantityRequestDto;
import io.github.senjar.bookstoreapp.model.User;
import io.github.senjar.bookstoreapp.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "shopping cart", description = "Operations for managing shopping cart")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/cart-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove cart item",
            description = "Removes a single item from the shopping cart",
            responses = {
                    @ApiResponse(responseCode = "204",
                            description = "Item successfully removed from the shopping cart"),
                    @ApiResponse(responseCode = "404", description = "Cart item not found")
            }
    )
    public void removeCartItem(Authentication authentication,@PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        shoppingCartService.removeCartItem(user.getId(), id);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    @Operation(
            summary = "Show the shopping cart",
            description = "Shows the items added to the shopping cart",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successfully retrieved the shopping cart"),
                    @ApiResponse(responseCode = "404", description = "Shopping cart not found")
            }
    )
    public ShoppingCartDto getShoppingCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.showCartItems(user.getId());
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/cart-items/{id}")
    @Operation(
            summary = "Update the item quantity",
            description = "Updates the quantity of a single item",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Quantity updated"),
                    @ApiResponse(responseCode = "404", description = "Cart item not found")
            }
    )
    public ShoppingCartDto updateItemQuantity(Authentication authentication,
                                              @PathVariable(name = "id") Long itemId,
                                              @Valid @RequestBody
                                                  UpdateQuantityRequestDto request) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.updateItemQuantity(user.getId(), itemId, request.quantity());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add a product to the shopping cart",
            description = "Adds a single product to the shopping cart",
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Product successfully added to the shopping cart"),
                    @ApiResponse(responseCode = "404", description = "Book not found")
            }
    )
    public ShoppingCartDto addToShoppingCart(Authentication authentication,
                                             @Valid @RequestBody ItemRequestDto item) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.addToShoppingCart(user.getId(), item);
    }

}
