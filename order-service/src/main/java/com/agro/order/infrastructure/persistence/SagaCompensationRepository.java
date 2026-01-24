package com.agro.order.infrastructure.persistence;

import com.agro.order.domain.SagaCompensation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaCompensationRepository extends JpaRepository<SagaCompensation, String> {
  Optional<SagaCompensation> findByOrderIdAndAction(String orderId, String action);
  long countByOrderId(String orderId);
}
