package com.agro.inventory.application;

import com.agro.inventory.domain.ProcessedCommand;
import com.agro.inventory.domain.Product;
import com.agro.inventory.domain.ReservationStatus;
import com.agro.inventory.domain.StockReservation;
import com.agro.inventory.infrastructure.kafka.InventoryEvent;
import com.agro.inventory.infrastructure.kafka.InventoryEventPublisher;
import com.agro.inventory.infrastructure.persistence.ProcessedCommandRepository;
import com.agro.inventory.infrastructure.persistence.ProductRepository;
import com.agro.inventory.infrastructure.persistence.StockReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class InventoryApplicationService {
  public record InventoryResult(boolean success, String message) {
  }

  private final ProductRepository productRepository;
  private final StockReservationRepository reservationRepository;
  private final ProcessedCommandRepository commandRepository;
  private final InventoryEventPublisher eventPublisher;

  public InventoryApplicationService(ProductRepository productRepository,
      StockReservationRepository reservationRepository,
      ProcessedCommandRepository commandRepository,
      InventoryEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.reservationRepository = reservationRepository;
    this.commandRepository = commandRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public InventoryResult reserve(String commandId, String orderId, String productId, int quantity) {
    if (commandRepository.existsById(commandId)) {
      return new InventoryResult(true, "Command already processed");
    }

    Product product = productRepository.findById(productId).orElse(null);
    if (product == null || product.getAvailableStock() < quantity) {
      return new InventoryResult(false, "Insufficient stock");
    }

    StockReservation reservation = reservationRepository.findByOrderIdAndProductId(orderId, productId);
    if (reservation != null && reservation.getStatus() == ReservationStatus.RESERVED) {
      return new InventoryResult(false, "Stock already reserved for order");
    }

    product.decrease(quantity);
    productRepository.save(product);

    if (reservation == null) {
      reservation = new StockReservation(UUID.randomUUID().toString(), orderId, productId, quantity,
          ReservationStatus.RESERVED);
    } else {
      reservation.setStatus(ReservationStatus.RESERVED);
    }
    reservationRepository.save(reservation);
    commandRepository.save(new ProcessedCommand(commandId, "RESERVE"));

    registerEventAfterCommit(new InventoryEvent(orderId, productId, quantity, "RESERVED"),
        InventoryEventPublisher.TOPIC_RESERVED);
    return new InventoryResult(true, "Stock reserved");
  }

  @Transactional
  public InventoryResult release(String commandId, String orderId, String productId) {
    if (commandRepository.existsById(commandId)) {
      return new InventoryResult(true, "Command already processed");
    }

    StockReservation reservation = reservationRepository.findByOrderIdAndProductId(orderId, productId);
    if (reservation == null) {
      return new InventoryResult(false, "Reservation not found");
    }

    if (reservation.getStatus() == ReservationStatus.RELEASED) {
      commandRepository.save(new ProcessedCommand(commandId, "RELEASE"));
      return new InventoryResult(true, "Stock already released");
    }

    Product product = productRepository.findById(productId).orElse(null);
    if (product == null) {
      return new InventoryResult(false, "Product not found");
    }

    product.increase(reservation.getQuantity());
    productRepository.save(product);

    reservation.setStatus(ReservationStatus.RELEASED);
    reservationRepository.save(reservation);
    commandRepository.save(new ProcessedCommand(commandId, "RELEASE"));

    registerEventAfterCommit(new InventoryEvent(orderId, productId, reservation.getQuantity(), "RELEASED"),
        InventoryEventPublisher.TOPIC_RELEASED);
    return new InventoryResult(true, "Stock released");
  }

  private void registerEventAfterCommit(InventoryEvent event, String topic) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventPublisher.publish(topic, event);
      }
    });
  }
}
