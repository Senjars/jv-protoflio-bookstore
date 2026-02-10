package io.github.senjar.bookstoreapp.dto.book;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;

@Data
public class BookDto {

    private Long id;
    private String title;
    private String author;
    private Set<Long> categoryIds;
    private String isbn;
    private String description;
    private BigDecimal price;
    private String coverImage;
}
