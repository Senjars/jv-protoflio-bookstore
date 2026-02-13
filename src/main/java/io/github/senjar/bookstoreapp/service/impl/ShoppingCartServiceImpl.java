package io.github.senjar.bookstoreapp.service.impl;

import io.github.senjar.bookstoreapp.dto.shoppingcart.ItemRequestDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.ShoppingCartMapper;
import io.github.senjar.bookstoreapp.model.Book;
import io.github.senjar.bookstoreapp.model.CartItem;
import io.github.senjar.bookstoreapp.model.ShoppingCart;
import io.github.senjar.bookstoreapp.model.User;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import io.github.senjar.bookstoreapp.repository.cartitem.CartItemRepository;
import io.github.senjar.bookstoreapp.repository.shoppingcart.ShoppingCartRepository;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import io.github.senjar.bookstoreapp.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new EntityNotFoundException("Can't find item with id: " + cartItemId));

        if (!cartItem.getShoppingCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only remove items from your own cart");
        }
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public ShoppingCartDto showCartItems(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ShoppingCart cart = new ShoppingCart();
                    User user = userRepository.findById(userId).orElseThrow(
                            () -> new EntityNotFoundException("User not found"));
                    cart.setUser(user);
                    return shoppingCartRepository.save(cart);
                });
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto updateItemQuantity(Long userId, Long cartItemId, int number) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();

        if (!cartItem.getShoppingCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own cart");
        }

        cartItem.setQuantity(number);
        return shoppingCartMapper.toDto(cartItem.getShoppingCart());
    }

    @Transactional
    public ShoppingCartDto addToShoppingCart(Long userId, ItemRequestDto itemRequest) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ShoppingCart newShoppingCart = new ShoppingCart();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));
                    newShoppingCart.setUser(user);
                    return shoppingCartRepository.save(newShoppingCart);
                });

        cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(itemRequest.productId()))
                .findFirst()
                .ifPresentOrElse(existingItem -> existingItem.setQuantity(
                        existingItem.getQuantity() + itemRequest.quantity()),
                        () -> {
                            Book book = bookRepository.findById(itemRequest.productId())
                                    .orElseThrow();
                            CartItem newItem = new CartItem();
                            newItem.setShoppingCart(cart);
                            newItem.setBook(book);
                            newItem.setQuantity(itemRequest.quantity());
                            cart.getCartItems().add(newItem);
                            cartItemRepository.save(newItem);
                        });
        return shoppingCartMapper.toDto(cart);
    }
}
