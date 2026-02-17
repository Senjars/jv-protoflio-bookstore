package io.github.senjar.bookstoreapp.repository.shoppingcart;

import io.github.senjar.bookstoreapp.model.ShoppingCart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long>,
        JpaSpecificationExecutor<ShoppingCart> {

    Optional<ShoppingCart> findByUserId(Long id);
}
