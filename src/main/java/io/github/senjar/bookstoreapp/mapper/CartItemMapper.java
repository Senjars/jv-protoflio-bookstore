package io.github.senjar.bookstoreapp.mapper;

import io.github.senjar.bookstoreapp.dto.shoppingcart.CartItemDto;
import io.github.senjar.bookstoreapp.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "bookTitle")
    CartItemDto toDto(CartItem cartItem);
}
