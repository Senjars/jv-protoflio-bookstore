package io.github.senjar.bookstoreapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.senjar.bookstoreapp.dto.shoppingcart.CartItemDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ItemRequestDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.exception.AccessDeniedException;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.mapper.ShoppingCartMapper;
import io.github.senjar.bookstoreapp.model.book.Book;
import io.github.senjar.bookstoreapp.model.cart.CartItem;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.repository.book.BookRepository;
import io.github.senjar.bookstoreapp.repository.shoppingcart.CartItemRepository;
import io.github.senjar.bookstoreapp.repository.shoppingcart.ShoppingCartRepository;
import io.github.senjar.bookstoreapp.repository.user.UserRepository;
import io.github.senjar.bookstoreapp.security.CustomUserDetailsService;
import io.github.senjar.bookstoreapp.security.JwtUtil;
import io.github.senjar.bookstoreapp.service.impl.ShoppingCartServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Shopping Cart Service Business Logic Test")
public class ShoppingCartServiceTest {

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should successfully delete cart item when user is the owner")
    void removeCartItem_validRequest_returnsNoContent() {
        Long userId = 2L;
        Long itemId = 1L;

        User user = mockUser(userId);
        ShoppingCart shoppingCart = mockCart(3L, user);
        CartItem cartItem = mockCartItem(itemId, shoppingCart);

        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(cartItem));

        shoppingCartService.removeCartItem(userId, itemId);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when trying to remove item that does not exist")
    void removeCartItem_invalidItemId_throwsEntityNotFoundException() {
        Long userId = 1L;
        Long invalidItemId = 999L;

        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartService.removeCartItem(userId, invalidItemId);
        });

        verify(cartItemRepository, never()).deleteById(invalidItemId);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user tries to remove someone else's cart item")
    void removeCartItem_userNotOwner_throwsAccessDeniedException() {
        Long userId = 1L;
        Long cartItemId = 10L;

        User otherUser = mockUser(2L);
        ShoppingCart shoppingCart = mockCart(3L, otherUser);
        CartItem cartItem = mockCartItem(cartItemId, shoppingCart);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        assertThrows(AccessDeniedException.class, () -> {
           shoppingCartService.removeCartItem(userId, cartItemId);
        });

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should retrieve shopping cart and map it to DTO for a given user ID")
    void showCartItems_validUserId_returnShoppingCartDto() {
        Long userId = 1L;
        User user = mockUser(userId);

        ShoppingCart shoppingCart = mockCart(2L, user);
        ShoppingCartDto expectedCartDto = new ShoppingCartDto();
        expectedCartDto.setUserId(userId);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expectedCartDto);

        ShoppingCartDto actual = shoppingCartService.showCartItems(userId);

        assertEquals(expectedCartDto.getUserId(), actual.getUserId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when shopping cart is not found for user")
    void showCartItems_cartNotFound_throwsEntityNotFoundException() {
        Long userId = 1L;
        when(shoppingCartRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.showCartItems(userId));

        verify(shoppingCartMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should successfully update item quantity when user is the owner")
    void updateItemQuantity_validRequest_returnsShoppingCartDto() {
        Long userId = 23L;
        Long cartItemId = 1L;

        User user = mockUser(userId);
        ShoppingCart shoppingCart = mockCart(32L, user);
        CartItem cartItem = mockCartItem(13L, shoppingCart);

        CartItemDto itemDto = new CartItemDto();
        itemDto.setQuantity(3);

        ShoppingCartDto expectedCartDto = new ShoppingCartDto();
        expectedCartDto.setId(32L);
        expectedCartDto.setCartItems(Set.of(itemDto));

        int newQuantity = 3;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(shoppingCartMapper.toDto(cartItem.getShoppingCart())).thenReturn(expectedCartDto);

        ShoppingCartDto actual = shoppingCartService
                .updateItemQuantity(userId, cartItemId, newQuantity);

        assertEquals(expectedCartDto.getCartItems(), actual.getCartItems());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating quantity of non-existing item")
    void updateItemQuantity_invalidItemId_throwsEntityNotFoundException() {
        Long userId = 1L;
        Long invalidItemId = 999L;
        int quantity = 13;

        when(cartItemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartService.updateItemQuantity(userId, invalidItemId, quantity);
        });
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user tries to update quantity of someone else's item")
    void updateItemQuantity_userNotOwner_throwsAccessDeniedException() {
        Long ownerId = 13L;
        User owner = new User();
        owner.setId(ownerId);

        Long otherUserId = 1L;

        Long cartItemId = 22L;
        int quantity = 2;
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(owner);

        cartItem.setShoppingCart(shoppingCart);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        assertThrows(AccessDeniedException.class, () -> {
            shoppingCartService.updateItemQuantity(otherUserId, cartItemId, quantity);
        });
    }

    @Test
    @DisplayName("Should increase quantity of existing item if the same book is added again")
    void addToShoppingCart_bookAlreadyInCart_updatesQuantity() {
        Long userId = 1L;
        Long bookId = 10L;
        ItemRequestDto itemRequest = new ItemRequestDto(bookId, 2);

        Book book = new Book();
        book.setId(bookId);

        CartItem existingItem = mockCartItem(1L, null);
        existingItem.setBook(book);
        existingItem.setQuantity(3);

        ShoppingCart cart = mockCart(1L, mockUser(userId));
        cart.setCartItems(new HashSet<>(Set.of(existingItem)));

        ShoppingCartDto expectedDto = new ShoppingCartDto();
        expectedDto.setId(1L);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(shoppingCartMapper.toDto(cart)).thenReturn(expectedDto);

        ShoppingCartDto actual = shoppingCartService.addToShoppingCart(userId, itemRequest);

        assertEquals(5, existingItem.getQuantity());
        assertEquals(expectedDto, actual);
    }

    @Test
    @DisplayName("Should create and add a new cart item when book is not already in the cart")
    void addToShoppingCart_bookNotInCart_addsNewItem() {
        Long userId = 1L;
        Long bookId = 10L;
        ItemRequestDto itemRequest = new ItemRequestDto(bookId, 2);

        Book book = new Book();
        book.setId(bookId);

        ShoppingCart cart = mockCart(1L, mockUser(userId));

        ShoppingCartDto expectedDto = new ShoppingCartDto();
        expectedDto.setId(1L);

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(shoppingCartMapper.toDto(cart)).thenReturn(expectedDto);

        ShoppingCartDto actual = shoppingCartService.addToShoppingCart(userId, itemRequest);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(expectedDto, actual);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when book does not exist")
    void addToShoppingCart_bookNotFound_throwsException() {
        Long userId = 1L;
        Long bookId = 999L;
        ItemRequestDto itemRequest = new ItemRequestDto(bookId, 2);

        ShoppingCart cart = mockCart(1L, mockUser(userId));

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addToShoppingCart(userId, itemRequest));

        verify(cartItemRepository, never()).save(any());
    }

    private User mockUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private ShoppingCart mockCart(Long id, User user) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(id);
        shoppingCart.setUser(user);
        shoppingCart.setCartItems(new HashSet<>());
        return shoppingCart;
    }

    private CartItem mockCartItem(Long id, ShoppingCart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setShoppingCart(cart);
        return cartItem;
    }
}
