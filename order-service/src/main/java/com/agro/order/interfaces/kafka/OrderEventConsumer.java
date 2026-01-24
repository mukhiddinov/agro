package com.agro.order.interfaces.kafka;

import com.agro.order.application.OrderSagaService;
import com.agro.order.application.SagaStateMachine.CompensationAction;
import com.agro.order.domain.SagaState;
import com.agro.order.infrastructure.kafka.InventoryEvent;
import com.agro.order.infrastructure.kafka.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {
  private final ObjectMapper objectMapper;
  private final OrderSagaService sagaService;

  public OrderEventConsumer(ObjectMapper objectMapper, OrderSagaService sagaService) {
    this.objectMapper = objectMapper;
    this.sagaService = sagaService;
  }

  @KafkaListener(topics = "inventory.stock.reserved", groupId = "order-service")
  public void onStockReserved(String payload) throws Exception {
    InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
    sagaService.applyProgressEvent(event.orderId(), SagaState.STARTED, SagaState.STOCK_RESERVED);
  }

  @KafkaListener(topics = "inventory.stock.released", groupId = "order-service")
  public void onStockReleased(String payload) throws Exception {
    InventoryEvent event = objectMapper.readValue(payload, InventoryEvent.class);
    sagaService.applyCompensationEvent(event.orderId(), CompensationAction.RELEASE_STOCK);
  }

  @KafkaListener(topics = "account.buyer.debited", groupId = "order-service")
  public void onBuyerDebited(String payload) throws Exception {
    // no-op (account service not part of saga)
  }

  @KafkaListener(topics = "account.buyer.refunded", groupId = "order-service")
  public void onBuyerRefunded(String payload) throws Exception {
    // no-op (account service not part of saga)
  }

  @KafkaListener(topics = "account.seller.credited", groupId = "order-service")
  public void onSellerCredited(String payload) throws Exception {
    // no-op (account service not part of saga)
  }

  @KafkaListener(topics = "account.seller.credit.reversed", groupId = "order-service")
  public void onSellerCreditReversed(String payload) throws Exception {
    // no-op (account service not part of saga)
  }

  @KafkaListener(topics = "payment.authorized", groupId = "order-service")
  public void onPaymentAuthorized(String payload) throws Exception {
    PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
    sagaService.applyProgressEvent(event.orderId(), SagaState.STOCK_RESERVED, SagaState.PAYMENT_AUTHORIZED);
  }

  @KafkaListener(topics = "payment.captured", groupId = "order-service")
  public void onPaymentCaptured(String payload) throws Exception {
    PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
    sagaService.applyProgressEvent(event.orderId(), SagaState.PAYMENT_AUTHORIZED, SagaState.PAYMENT_CAPTURED);
  }

  @KafkaListener(topics = "payment.refunded", groupId = "order-service")
  public void onPaymentRefunded(String payload) throws Exception {
    PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
    sagaService.applyCompensationEvent(event.orderId(), CompensationAction.REFUND_PAYMENT);
  }
}
