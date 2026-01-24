package com.agro.payment.application;

import com.agro.payment.domain.Payment;
import com.agro.payment.domain.PaymentStatus;
import com.agro.payment.domain.ProcessedCommand;
import com.agro.payment.infrastructure.kafka.PaymentEvent;
import com.agro.payment.infrastructure.kafka.PaymentEventPublisher;
import com.agro.payment.infrastructure.persistence.PaymentRepository;
import com.agro.payment.infrastructure.persistence.ProcessedCommandRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PaymentApplicationService {
  public record PaymentResult(boolean success, String message, Payment payment) {
  }

  private final PaymentRepository paymentRepository;
  private final ProcessedCommandRepository commandRepository;
  private final PaymentEventPublisher eventPublisher;

  public PaymentApplicationService(PaymentRepository paymentRepository,
      ProcessedCommandRepository commandRepository,
      PaymentEventPublisher eventPublisher) {
    this.paymentRepository = paymentRepository;
    this.commandRepository = commandRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public PaymentResult authorize(String commandId, String orderId, String buyerId,
      String paymentMethodId, BigDecimal amount, String currency) {
    if ("PM-FAIL".equalsIgnoreCase(paymentMethodId)) {
      return new PaymentResult(false, "Payment authorization failed", null);
    }

    if (commandRepository.existsById(commandId)) {
      return new PaymentResult(true, "Command already processed", null);
    }

    Optional<Payment> existing = paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId);
    if (existing.isPresent()) {
      Payment payment = existing.get();
      if (payment.getStatus() == PaymentStatus.AUTHORIZED || payment.getStatus() == PaymentStatus.CAPTURED) {
        commandRepository.save(new ProcessedCommand(commandId, "AUTHORIZE"));
        return new PaymentResult(true, "Payment already authorized", payment);
      }
      return new PaymentResult(false, "Payment already exists in status " + payment.getStatus(), payment);
    }

    Instant now = Instant.now();
    Payment payment = new Payment(UUID.randomUUID().toString(), orderId, buyerId, paymentMethodId,
        amount, currency, PaymentStatus.AUTHORIZED, now, now);
    paymentRepository.save(payment);
    commandRepository.save(new ProcessedCommand(commandId, "AUTHORIZE"));
    registerEventAfterCommit(PaymentEventPublisher.TOPIC_AUTHORIZED,
        new PaymentEvent(orderId, payment.getId(), amount, "AUTHORIZED"));
    return new PaymentResult(true, "Authorized", payment);
  }

  @Transactional
  public PaymentResult capture(String commandId, String orderId, BigDecimal amount, String currency) {
    if (commandRepository.existsById(commandId)) {
      return new PaymentResult(true, "Command already processed", null);
    }

    Payment payment = paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
    if (payment == null) {
      return new PaymentResult(false, "Payment not found", null);
    }

    if (payment.getStatus() == PaymentStatus.CAPTURED) {
      commandRepository.save(new ProcessedCommand(commandId, "CAPTURE"));
      return new PaymentResult(true, "Already captured", payment);
    }

    if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
      return new PaymentResult(false, "Payment not authorized", payment);
    }

    if (payment.getAmount().compareTo(amount) != 0 || !payment.getCurrency().equals(currency)) {
      return new PaymentResult(false, "Amount or currency mismatch", payment);
    }

    payment.setStatus(PaymentStatus.CAPTURED);
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);
    commandRepository.save(new ProcessedCommand(commandId, "CAPTURE"));
    registerEventAfterCommit(PaymentEventPublisher.TOPIC_CAPTURED,
        new PaymentEvent(orderId, payment.getId(), amount, "CAPTURED"));
    return new PaymentResult(true, "Captured", payment);
  }

  @Transactional
  public PaymentResult refund(String commandId, String orderId, BigDecimal amount, String currency) {
    if (commandRepository.existsById(commandId)) {
      return new PaymentResult(true, "Command already processed", null);
    }

    Payment payment = paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
    if (payment == null) {
      return new PaymentResult(false, "Payment not found", null);
    }

    if (payment.getStatus() == PaymentStatus.REFUNDED) {
      commandRepository.save(new ProcessedCommand(commandId, "REFUND"));
      return new PaymentResult(true, "Already refunded", payment);
    }

    if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
      return new PaymentResult(false, "Payment not refundable", payment);
    }

    if (payment.getAmount().compareTo(amount) != 0 || !payment.getCurrency().equals(currency)) {
      return new PaymentResult(false, "Amount or currency mismatch", payment);
    }

    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);
    commandRepository.save(new ProcessedCommand(commandId, "REFUND"));
    registerEventAfterCommit(PaymentEventPublisher.TOPIC_REFUNDED,
        new PaymentEvent(orderId, payment.getId(), amount, "REFUNDED"));
    return new PaymentResult(true, "Refunded", payment);
  }

  private void registerEventAfterCommit(String topic, PaymentEvent event) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventPublisher.publish(topic, event);
      }
    });
  }
}
