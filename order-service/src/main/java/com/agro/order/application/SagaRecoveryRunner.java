package com.agro.order.application;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SagaRecoveryRunner {
  private final OrderSagaService sagaService;

  public SagaRecoveryRunner(OrderSagaService sagaService) {
    this.sagaService = sagaService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void recover() {
    sagaService.recoverIncompleteSagas();
  }
}
