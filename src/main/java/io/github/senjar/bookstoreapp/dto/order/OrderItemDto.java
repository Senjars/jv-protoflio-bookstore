package io.github.senjar.bookstoreapp.dto.order;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemDto {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private int quantity;
    private BigDecimal price;
}
