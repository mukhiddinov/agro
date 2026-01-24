package com.agro.order.domain;

public enum SagaState {
  STARTED,
  STOCK_RESERVED,
  PAYMENT_AUTHORIZED,
  PAYMENT_CAPTURED,
  COMPLETED,
  FAILED,
  COMPENSATING
}
