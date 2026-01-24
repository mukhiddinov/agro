package com.agro.order.application;

import com.agro.order.domain.SagaInstance;
import com.agro.order.domain.SagaState;
import com.agro.order.infrastructure.persistence.SagaInstanceRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SagaTimeoutScheduler {
  private final SagaInstanceRepository sagaRepository;
  private final OrderSagaService sagaService;
  private final SagaStateMachine stateMachine;

  public SagaTimeoutScheduler(SagaInstanceRepository sagaRepository,
      OrderSagaService sagaService,
      SagaStateMachine stateMachine) {
    this.sagaRepository = sagaRepository;
    this.sagaService = sagaService;
    this.stateMachine = stateMachine;
  }

  @Scheduled(fixedDelayString = "30000")
  public void detectStuckSagas() {
    Instant now = Instant.now();
    List<SagaInstance> candidates = sagaRepository.findByStateNotIn(
        List.of(SagaState.COMPLETED, SagaState.FAILED));
    for (SagaInstance saga : candidates) {
      if (saga.getLastUpdatedAt() == null) {
        continue;
      }
      if (now.isAfter(saga.getLastUpdatedAt().plus(stateMachine.timeoutFor(saga.getState())))) {
        sagaService.handleTimeout(saga);
      }
    }
  }
}
