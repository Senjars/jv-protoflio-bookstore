package io.github.senjar.bookstoreapp.dto.order;

import ch.qos.logback.core.status.Status;
import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequestDto {

    @NotBlank
    private Status status;
}
