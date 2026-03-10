package io.github.senjar.bookstoreapp.dto.shoppingcart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemRequestDto(

        @NotNull
        Long bookId,

        @Min(1)
        int quantity
) {
}
