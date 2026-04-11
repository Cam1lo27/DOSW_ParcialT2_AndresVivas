package edu.dosw.parcial.persistence.repositories;

import edu.dosw.parcial.core.models.OrderStatusEnum;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import edu.dosw.parcial.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByUserAndStatus(UserEntity user, OrderStatusEnum status);
    boolean existsByUserAndStatus(UserEntity user, OrderStatusEnum status);
}