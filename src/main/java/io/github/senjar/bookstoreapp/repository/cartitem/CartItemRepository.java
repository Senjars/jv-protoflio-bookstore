package io.github.senjar.bookstoreapp.repository.cartitem;

import io.github.senjar.bookstoreapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
