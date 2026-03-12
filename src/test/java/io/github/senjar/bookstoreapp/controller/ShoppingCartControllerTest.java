package io.github.senjar.bookstoreapp.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.senjar.bookstoreapp.dto.shoppingcart.ItemRequestDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.dto.shoppingcart.UpdateQuantityRequestDto;
import io.github.senjar.bookstoreapp.exception.EntityNotFoundException;
import io.github.senjar.bookstoreapp.model.user.Role;
import io.github.senjar.bookstoreapp.model.user.RoleName;
import io.github.senjar.bookstoreapp.model.user.User;
import io.github.senjar.bookstoreapp.security.CustomUserDetailsService;
import io.github.senjar.bookstoreapp.security.JwtUtil;
import io.github.senjar.bookstoreapp.service.ShoppingCartService;

@WebMvcTest(value = ShoppingCartController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@DisplayName("ShoppingCart Controller Tests")
public class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should successfully remove item from shopping cart")
    void removeCartItem_validRequest_returnsNoContent() throws Exception {
        User user = mockUser(1L);
        Long cartItemId = 23L;

        mockMvc.perform(delete("/api/cart/cart-items/{id}", cartItemId)
                        .with(user(user))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(shoppingCartService).removeCartItem(user.getId(), cartItemId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to remove non-existing cart item")
    void removeCartItem_invalidItemId_throwsEntityNotFoundException() throws Exception {
        User user = mockUser(1L);
        Long invalidItemId = 999L;

        doThrow(new EntityNotFoundException("Item with id " + invalidItemId + " not found"))
                .when(shoppingCartService).removeCartItem(user.getId(), invalidItemId);

        mockMvc.perform(delete("/api/cart/cart-items/{id}", invalidItemId)
                        .with(user(user))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return current user's shopping cart with correct ID")
    void getShoppingCart_validRequest_returnsShoppingCartDto() throws Exception {
        User user = mockUser(1L);
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setId(1L);

        when(shoppingCartService.showCartItems(user.getId())).thenReturn(shoppingCartDto);

        mockMvc.perform(get("/api/cart")
                        .with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should successfully update item quantity and return updated cart")
    void updateItemQuantity_validItemId_returnsShoppingCartDto() throws Exception {
        User user = mockUser(1L);
        Long itemId = 23L;

        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(3);

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setId(1L);

        when(shoppingCartService.updateItemQuantity(user.getId(), itemId, 3))
                .thenReturn(shoppingCartDto);

        mockMvc.perform(put("/api/cart/cart-items/{id}", itemId)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to update quantity of non-existing item")
    void updateItemQuantity_invalidItemId_throwsEntityNotFoundException() throws Exception {
        User user = mockUser(1L);
        Long invalidItemId = 999L;

        UpdateQuantityRequestDto requestDto = new UpdateQuantityRequestDto(3);

        doThrow(new EntityNotFoundException("Item with id " + invalidItemId + " not found"))
                .when(shoppingCartService).updateItemQuantity(user.getId(), invalidItemId, 3);

        mockMvc.perform(put("/api/cart/cart-items/{id}", invalidItemId)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when provided quantity is invalid (e.g. zero)")
    void updateItemQuantity_invalidQuantity_returnsBadRequest() throws Exception {
        User user = mockUser(1L);
        Long itemId = 23L;

        UpdateQuantityRequestDto invalidRequest = new UpdateQuantityRequestDto(0);

        mockMvc.perform(put("/api/cart/cart-items/{id}", itemId)
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully add a new book to the shopping cart")
    void addToShoppingCart_validRequest_returnsShoppingCartDto() throws Exception {
        User user = mockUser(1L);
        ItemRequestDto itemRequest = new ItemRequestDto(10L, 2);

        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setId(1L);

        when(shoppingCartService.addToShoppingCart(user.getId(), itemRequest))
                .thenReturn(shoppingCartDto);

        mockMvc.perform(post("/api/cart")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when item request data is incomplete or invalid")
    void addToShoppingCart_invalidRequest_returnsBadRequest() throws Exception {
        User user = mockUser(1L);
        ItemRequestDto invalidRequest = new ItemRequestDto(null, 0);

        mockMvc.perform(post("/api/cart")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 Not Found when adding non-existing book")
    void addToShoppingCart_bookNotFound_returnsNotFound() throws Exception {
        User user = mockUser(1L);
        Long bookId = 999L;
        ItemRequestDto requestDto = new ItemRequestDto(bookId, 2);

        when(shoppingCartService.addToShoppingCart(user.getId(), requestDto))
                .thenThrow(new EntityNotFoundException("Book not found"));

        mockMvc.perform(post("/api/cart")
                        .with(user(user))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
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

}
