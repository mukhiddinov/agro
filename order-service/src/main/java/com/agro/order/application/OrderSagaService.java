package com.agro.order.application;

import com.agro.contracts.CommandReply;
import com.agro.order.application.SagaStateMachine.CompensationAction;
import com.agro.order.application.SagaStateMachine.SagaCommand;
import com.agro.order.application.SagaStateMachine.Transition;
import com.agro.order.domain.Order;
import com.agro.order.domain.OrderStatus;
import com.agro.order.domain.SagaCompensation;
import com.agro.order.domain.SagaInstance;
import com.agro.order.domain.SagaState;
import com.agro.order.infrastructure.grpc.InventoryGrpcClient;
import com.agro.order.infrastructure.grpc.PaymentGrpcClient;
import com.agro.order.infrastructure.http.ShippingClient;
import com.agro.order.infrastructure.kafka.SagaEvent;
import com.agro.order.infrastructure.kafka.SagaEventPublisher;
import com.agro.order.infrastructure.persistence.OrderRepository;
import com.agro.order.infrastructure.persistence.SagaCompensationRepository;
import com.agro.order.infrastructure.persistence.SagaInstanceRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderSagaService {
  private static final int MAX_RETRY_ATTEMPTS = 3;
  private final OrderRepository orderRepository;
  private final SagaInstanceRepository sagaRepository;
  private final InventoryGrpcClient inventoryClient;
  private final PaymentGrpcClient paymentClient;
  private final ShippingClient shippingClient;
  private final SagaEventPublisher eventPublisher;
  private final SagaStateMachine stateMachine;
  private final SagaCompensationRepository compensationRepository;

  public OrderSagaService(
      OrderRepository orderRepository,
      SagaInstanceRepository sagaRepository,
      InventoryGrpcClient inventoryClient,
      PaymentGrpcClient paymentClient,
      ShippingClient shippingClient,
      SagaEventPublisher eventPublisher,
      SagaStateMachine stateMachine,
      SagaCompensationRepository compensationRepository) {
    this.orderRepository = orderRepository;
    this.sagaRepository = sagaRepository;
    this.inventoryClient = inventoryClient;
    this.paymentClient = paymentClient;
    this.shippingClient = shippingClient;
    this.eventPublisher = eventPublisher;
    this.stateMachine = stateMachine;
    this.compensationRepository = compensationRepository;
  }

  public String placeOrder(PlaceOrderCommand command) {
    String orderId = UUID.randomUUID().toString();
    Instant now = Instant.now();
    Order order = new Order(orderId, command.productId(), command.quantity(), command.buyerId(),
        command.amount(), command.currency(), command.paymentMethodId(), command.shippingOptionId(),
        command.addressId(), OrderStatus.PENDING, now, now);
    orderRepository.save(order);

    SagaInstance saga = new SagaInstance(orderId, SagaState.STARTED);
    sagaRepository.save(saga);
    eventPublisher.publish(SagaEventPublisher.TOPIC_SAGA_STARTED,
        new SagaEvent(orderId, "START", "STARTED", "Saga started"));

    executeSaga(order, saga);
    return orderId;
  }

  public void executeSaga(Order order, SagaInstance saga) {
    if (saga.getState() == SagaState.COMPENSATING) {
      executeCompensations(stateMachine.compensationPlan(saga.getLastSuccessfulState()), order);
      failSaga(order, saga, saga.getLastSuccessfulState().name(), "Compensation completed");
      return;
    }

    while (!stateMachine.isTerminal(saga.getState())) {
      SagaState currentState = saga.getState();
      SagaCommand command = stateMachine.nextCommand(currentState);

      if (command == SagaCommand.COMPLETE) {
        Transition transition = stateMachine.onSuccess(currentState);
        updateSagaState(saga, transition.nextState());
        completeOrder(order, saga);
        return;
      }

      if (command.name().equals(saga.getPendingCommand())) {
        return;
      }

      CommandReply reply = executeCommandWithRetry(command, order, saga);
      recordStep(command.name(), reply, order.getId());

      if (reply.getSuccess()) {
        return;
      } else {
        Transition transition = stateMachine.onFailure(currentState);
        if (transition.nextState() == SagaState.COMPENSATING) {
          updateSagaState(saga, SagaState.COMPENSATING);
          eventPublisher.publish(SagaEventPublisher.TOPIC_SAGA_COMPENSATING,
              new SagaEvent(order.getId(), command.name(), "COMPENSATING", reply.getMessage()));
          executeCompensations(transition.compensations(), order);
        }
        failSaga(order, saga, command.name(), reply.getMessage());
        return;
      }
    }
  }

  private CommandReply executeCommand(SagaCommand command, Order order, SagaInstance saga) {
    String commandId = resolveCommandId(saga, command);
    return switch (command) {
      case RESERVE_STOCK -> inventoryClient.reserve(commandId, order.getId(),
          order.getProductId(), order.getQuantity());
      case AUTHORIZE_PAYMENT -> paymentClient.authorize(commandId, order.getId(), order.getBuyerId(),
          order.getPaymentMethodId(), order.getAmount(), order.getCurrency());
      case CAPTURE_PAYMENT -> paymentClient.capture(commandId, order.getId(), order.getAmount(),
          order.getCurrency());
      case COMPLETE -> CommandReply.newBuilder().setSuccess(true).setMessage("Completed").build();
    };
  }

  private CommandReply executeCommandWithRetry(SagaCommand command, Order order, SagaInstance saga) {
    while (true) {
      try {
        CommandReply reply = executeCommand(command, order, saga);
        resetRetryState(saga);
        return reply;
      } catch (StatusRuntimeException ex) {
        if (!stateMachine.isRetryableState(saga.getState()) || !isTransient(ex)) {
          recordRetryFailure(saga, ex);
          return CommandReply.newBuilder()
              .setSuccess(false)
              .setMessage("Transient gRPC failure: " + ex.getStatus().getCode())
              .build();
        }

        int nextAttempt = saga.getRetryCount() + 1;
        if (nextAttempt > MAX_RETRY_ATTEMPTS) {
          recordRetryFailure(saga, ex);
          return CommandReply.newBuilder()
              .setSuccess(false)
              .setMessage("Retry limit reached: " + ex.getStatus().getCode())
              .build();
        }

        saga.setRetryCount(nextAttempt);
        saga.setLastError(ex.getStatus().getCode().name());
        saga.setLastUpdatedAt(Instant.now());
        sagaRepository.save(saga);
      }
    }
  }

  private boolean isTransient(StatusRuntimeException ex) {
    Status.Code code = ex.getStatus().getCode();
    return code == Status.Code.UNAVAILABLE
        || code == Status.Code.DEADLINE_EXCEEDED
        || code == Status.Code.RESOURCE_EXHAUSTED;
  }

  private void executeCompensations(Iterable<CompensationAction> actions, Order order) {
    for (CompensationAction action : actions) {
      switch (action) {
        case RELEASE_STOCK -> inventoryClient.release(newCommandId(), order.getId(),
            order.getProductId(), order.getQuantity());
        case REFUND_PAYMENT -> paymentClient.refund(newCommandId(), order.getId(),
            order.getAmount(), order.getCurrency());
      }
    }
  }

  private void recordStep(String step, CommandReply reply, String orderId) {
    String topic = reply.getSuccess()
        ? SagaEventPublisher.TOPIC_SAGA_STEP_COMPLETED
        : SagaEventPublisher.TOPIC_SAGA_STEP_FAILED;
    eventPublisher.publish(topic,
        new SagaEvent(orderId, step, reply.getSuccess() ? "COMPLETED" : "FAILED", reply.getMessage()));
  }

  @Transactional
  public void applyProgressEvent(String orderId, SagaState expectedState, SagaState nextState) {
    SagaInstance saga = sagaRepository.findById(orderId).orElse(null);
    if (saga == null) {
      return;
    }
    SagaState currentState = saga.getState();
    if (stateMachine.isTerminal(currentState) || currentState == SagaState.COMPENSATING) {
      return;
    }
    if (currentState.ordinal() >= nextState.ordinal()) {
      return;
    }
    updateSagaState(saga, nextState);
    clearPendingCommand(saga);
    Order order = orderRepository.findById(orderId).orElse(null);
    if (order == null) {
      return;
    }
    executeSaga(order, saga);
  }

  @Transactional
  public void applyCompensationEvent(String orderId, CompensationAction action) {
    SagaInstance saga = sagaRepository.findById(orderId).orElse(null);
    if (saga == null || saga.getState() != SagaState.COMPENSATING) {
      return;
    }

    if (stateMachine.compensationPlan(saga.getLastSuccessfulState()).stream()
        .noneMatch(plan -> plan == action)) {
      return;
    }

    compensationRepository.findByOrderIdAndAction(orderId, action.name())
        .orElseGet(() -> compensationRepository.save(
            new SagaCompensation(UUID.randomUUID().toString(), orderId, action.name())));

    long completed = compensationRepository.countByOrderId(orderId);
    if (completed >= stateMachine.compensationPlan(saga.getLastSuccessfulState()).size()) {
      Order order = orderRepository.findById(orderId).orElse(null);
      if (order != null) {
        failSaga(order, saga, "COMPENSATION", "Compensation completed");
      }
    }
  }

  @Transactional
  public void recoverIncompleteSagas() {
    for (SagaInstance saga : sagaRepository.findByStateNotIn(
        java.util.List.of(SagaState.COMPLETED, SagaState.FAILED))) {
      Order order = orderRepository.findById(saga.getOrderId()).orElse(null);
      if (order != null) {
        executeSaga(order, saga);
      }
    }
  }

  protected void failSaga(Order order, SagaInstance saga, String failedStep, String message) {
    order.setStatus(OrderStatus.FAILED);
    order.setUpdatedAt(Instant.now());
    orderRepository.save(order);
    updateSagaState(saga, SagaState.FAILED);
    clearPendingCommand(saga);
    eventPublisher.publish(SagaEventPublisher.TOPIC_SAGA_STEP_FAILED,
        new SagaEvent(order.getId(), failedStep, "FAILED", message));
  }

  protected void completeOrder(Order order, SagaInstance saga) {
    order.setStatus(OrderStatus.COMPLETED);
    order.setUpdatedAt(Instant.now());
    orderRepository.save(order);
    updateSagaState(saga, SagaState.COMPLETED);
    clearPendingCommand(saga);
    eventPublisher.publish(SagaEventPublisher.TOPIC_SAGA_COMPLETED,
        new SagaEvent(order.getId(), "COMPLETE", "COMPLETED", "Order completed"));

    if (shippingClient.createShipment(order.getId(), order.getAddressId(),
        order.getShippingOptionId())) {
      order.setStatus(OrderStatus.READY_FOR_SHIPMENT);
      order.setUpdatedAt(Instant.now());
      orderRepository.save(order);
    }
  }

  private void updateSagaState(SagaInstance saga, SagaState state) {
    saga.setState(state);
    if (stateMachine.isProgressState(state)) {
      saga.setLastSuccessfulState(state);
    }
    resetRetryState(saga);
    saga.setLastUpdatedAt(Instant.now());
    sagaRepository.save(saga);
  }

  private String resolveCommandId(SagaInstance saga, SagaCommand command) {
    if (command.name().equals(saga.getPendingCommand()) && saga.getPendingCommandId() != null) {
      return saga.getPendingCommandId();
    }
    String commandId = newCommandId();
    saga.setPendingCommand(command.name());
    saga.setPendingCommandId(commandId);
    saga.setLastUpdatedAt(Instant.now());
    sagaRepository.save(saga);
    return commandId;
  }

  private void clearPendingCommand(SagaInstance saga) {
    saga.setPendingCommand(null);
    saga.setPendingCommandId(null);
    resetRetryState(saga);
    saga.setLastUpdatedAt(Instant.now());
    sagaRepository.save(saga);
  }

  private void resetRetryState(SagaInstance saga) {
    saga.setRetryCount(0);
    saga.setLastError(null);
  }

  private void recordRetryFailure(SagaInstance saga, StatusRuntimeException ex) {
    saga.setLastError(ex.getStatus().getCode().name());
    saga.setLastUpdatedAt(Instant.now());
    sagaRepository.save(saga);
  }

  @Transactional
  public void handleTimeout(SagaInstance saga) {
    if (stateMachine.isTerminal(saga.getState())) {
      return;
    }

    Order order = orderRepository.findById(saga.getOrderId()).orElse(null);
    if (order == null) {
      return;
    }

    Transition transition = stateMachine.onFailure(saga.getState());
    if (transition.nextState() == SagaState.COMPENSATING) {
      updateSagaState(saga, SagaState.COMPENSATING);
      eventPublisher.publish(SagaEventPublisher.TOPIC_SAGA_COMPENSATING,
          new SagaEvent(order.getId(), "TIMEOUT", "COMPENSATING", "Saga timeout"));
      executeCompensations(transition.compensations(), order);
      failSaga(order, saga, "TIMEOUT", "Saga timeout");
    } else {
      failSaga(order, saga, "TIMEOUT", "Saga timeout");
    }
  }

  private String newCommandId() {
    return UUID.randomUUID().toString();
  }
}
