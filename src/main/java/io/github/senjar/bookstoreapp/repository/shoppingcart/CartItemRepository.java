package io.github.senjar.bookstoreapp.repository.shoppingcart;

import io.github.senjar.bookstoreapp.model.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
