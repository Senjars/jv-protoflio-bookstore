package io.github.senjar.bookstoreapp.dto.book;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
}
