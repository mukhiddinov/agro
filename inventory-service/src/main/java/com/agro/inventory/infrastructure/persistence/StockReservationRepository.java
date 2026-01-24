package com.agro.inventory.infrastructure.persistence;

import com.agro.inventory.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, String> {
  StockReservation findByOrderIdAndProductId(String orderId, String productId);
}
