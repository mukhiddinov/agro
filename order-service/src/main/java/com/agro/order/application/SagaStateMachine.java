package com.agro.order.application;

import com.agro.order.domain.SagaState;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SagaStateMachine {
  public enum SagaCommand {
    RESERVE_STOCK,
    AUTHORIZE_PAYMENT,
    CAPTURE_PAYMENT,
    COMPLETE
  }

  public enum CompensationAction {
    RELEASE_STOCK,
    REFUND_PAYMENT
  }

  public record Transition(SagaState nextState, List<CompensationAction> compensations) {
  }

  public SagaCommand nextCommand(SagaState state) {
    return switch (state) {
      case STARTED -> SagaCommand.RESERVE_STOCK;
      case STOCK_RESERVED -> SagaCommand.AUTHORIZE_PAYMENT;
      case PAYMENT_AUTHORIZED -> SagaCommand.CAPTURE_PAYMENT;
      case PAYMENT_CAPTURED -> SagaCommand.COMPLETE;
      case COMPLETED, FAILED, COMPENSATING -> SagaCommand.COMPLETE;
    };
  }

  public Transition onSuccess(SagaState state) {
    return switch (state) {
      case STARTED -> new Transition(SagaState.STOCK_RESERVED, List.of());
      case STOCK_RESERVED -> new Transition(SagaState.PAYMENT_AUTHORIZED, List.of());
      case PAYMENT_AUTHORIZED -> new Transition(SagaState.PAYMENT_CAPTURED, List.of());
      case PAYMENT_CAPTURED -> new Transition(SagaState.COMPLETED, List.of());
      case COMPLETED, FAILED, COMPENSATING -> new Transition(state, List.of());
    };
  }

  public Transition onFailure(SagaState state) {
    return switch (state) {
      case STARTED -> new Transition(SagaState.FAILED, List.of());
      case STOCK_RESERVED, PAYMENT_AUTHORIZED, PAYMENT_CAPTURED ->
          new Transition(SagaState.COMPENSATING, compensationPlan(state));
      case COMPLETED, FAILED, COMPENSATING -> new Transition(state, List.of());
    };
  }

  public List<CompensationAction> compensationPlan(SagaState lastSuccessfulState) {
    return switch (lastSuccessfulState) {
      case STOCK_RESERVED -> List.of(CompensationAction.RELEASE_STOCK);
      case PAYMENT_AUTHORIZED -> List.of(CompensationAction.REFUND_PAYMENT, CompensationAction.RELEASE_STOCK);
      case PAYMENT_CAPTURED -> List.of(CompensationAction.REFUND_PAYMENT, CompensationAction.RELEASE_STOCK);
      default -> List.of();
    };
  }

  public boolean isProgressState(SagaState state) {
    return state == SagaState.STARTED
        || state == SagaState.STOCK_RESERVED
        || state == SagaState.PAYMENT_AUTHORIZED
        || state == SagaState.PAYMENT_CAPTURED
        || state == SagaState.COMPLETED;
  }

  public boolean isTerminal(SagaState state) {
    return state == SagaState.COMPLETED || state == SagaState.FAILED;
  }

  public boolean isRetryableState(SagaState state) {
    return state == SagaState.STARTED
        || state == SagaState.STOCK_RESERVED
        || state == SagaState.PAYMENT_AUTHORIZED
        || state == SagaState.PAYMENT_CAPTURED;
  }

  public Duration timeoutFor(SagaState state) {
    return switch (state) {
      case STARTED, STOCK_RESERVED, PAYMENT_AUTHORIZED, PAYMENT_CAPTURED, COMPENSATING -> Duration.ofMinutes(2);
      case COMPLETED, FAILED -> Duration.ZERO;
    };
  }
}
