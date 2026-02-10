package io.github.senjar.bookstoreapp.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;

@Data
public class CreateBookRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    private String description;
    private Set<Long> categoryIds;

    @NotNull
    @Positive
    private BigDecimal price;

    private String coverImage;
}
