package io.github.senjar.bookstoreapp.dto.shoppingcart;

import jakarta.validation.constraints.Positive;

public record UpdateQuantityRequestDto(
        @Positive(message = "Quantity must be greater than 0")
        int quantity
) {
}
