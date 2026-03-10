package io.github.senjar.bookstoreapp.dto.book;

import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BookDtoWithoutCategoryIds {

    private String title;
    private String author;
    private String isbn;
    private String description;
    private BigDecimal price;
    private String coverImage;
}
