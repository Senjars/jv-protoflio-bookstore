package io.github.senjar.bookstoreapp.mapper;

import io.github.senjar.bookstoreapp.dto.shoppingcart.ShoppingCartDto;
import io.github.senjar.bookstoreapp.model.cart.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface ShoppingCartMapper {

    @Mapping(source = "user.id", target = "userId")
    ShoppingCartDto toDto(ShoppingCart shoppingCart);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    ShoppingCart toEntity(ShoppingCartDto shoppingCartDto);
}
