package io.github.senjar.bookstoreapp.repository.order;

import io.github.senjar.bookstoreapp.model.OrderItem;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {"book"})
    Set<OrderItem> findAllByOrderId(Long orderId);
}
