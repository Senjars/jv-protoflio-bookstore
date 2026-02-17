package io.github.senjar.bookstoreapp.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequestDto {

    @NotBlank
    private String shippingAddress;
}
