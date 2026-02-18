package io.github.senjar.bookstoreapp.dto.order;

import io.github.senjar.bookstoreapp.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequestDto {

    @NotNull
    private Status status;
}
