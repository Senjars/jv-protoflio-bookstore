package io.github.senjar.bookstoreapp.dto.shoppingcart;

public record ItemRequestDto(
        Long productId,
        int quantity
) {
}
