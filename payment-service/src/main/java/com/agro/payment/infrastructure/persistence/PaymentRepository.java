package com.agro.payment.infrastructure.persistence;

import com.agro.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
  Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(String orderId);
}
