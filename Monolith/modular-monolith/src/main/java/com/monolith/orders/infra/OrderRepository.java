package com.monolith.orders.infra;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = {"items"})
    Optional<OrderEntity> findWithItemsById(UUID id);

    @EntityGraph(attributePaths = {"items"})
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
