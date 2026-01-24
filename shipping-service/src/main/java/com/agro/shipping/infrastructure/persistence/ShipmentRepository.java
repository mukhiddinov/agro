package com.agro.shipping.infrastructure.persistence;

import com.agro.shipping.domain.Shipment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {
  Optional<Shipment> findByOrderId(String orderId);
}
