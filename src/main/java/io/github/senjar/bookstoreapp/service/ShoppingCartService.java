package io.github.senjar.bookstoreapp.service;

import io.github.senjar.bookstoreapp.dto.shoppingcart.ItemRequestDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;

public interface ShoppingCartService {

    void removeCartItem(Long userId,Long carItemId);

    ShoppingCartDto showCartItems(Long userId);

    ShoppingCartDto updateItemQuantity(Long userId, Long cartItemId, int number);

    ShoppingCartDto addToShoppingCart(Long userId, ItemRequestDto item);

    ShoppingCart getOrCreateCart(Long userId);
}
