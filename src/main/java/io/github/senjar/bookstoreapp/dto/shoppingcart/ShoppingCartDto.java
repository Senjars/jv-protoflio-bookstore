package io.github.senjar.bookstoreapp.dto.shoppingcart;

import java.util.Set;
import lombok.Data;

@Data
public class ShoppingCartDto {

    private Long id;
    private Long userId;
    private Set<CartItemDto> cartItems;
}
