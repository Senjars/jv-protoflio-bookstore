package io.github.senjar.bookstoreapp.repository.order;

import io.github.senjar.bookstoreapp.model.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "orderItems"})
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}
