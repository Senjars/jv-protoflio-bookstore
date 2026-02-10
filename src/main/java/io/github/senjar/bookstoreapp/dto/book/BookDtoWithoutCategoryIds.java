package io.github.senjar.bookstoreapp.dto.book;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BookDtoWithoutCategoryIds {

    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private String coverImage;
}
