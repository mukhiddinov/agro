package com.agro.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "saga_instances")
public class SagaInstance {
  @Id
  private String orderId;

  @Enumerated(EnumType.STRING)
  private SagaState state;

  @Enumerated(EnumType.STRING)
  private SagaState lastSuccessfulState;

  private String pendingCommand;
  private String pendingCommandId;

  private int retryCount;
  private String lastError;

  private Instant lastUpdatedAt;

  protected SagaInstance() {
  }

  public SagaInstance(String orderId, SagaState state) {
    this.orderId = orderId;
    this.state = state;
    this.lastSuccessfulState = state;
    this.retryCount = 0;
    this.lastUpdatedAt = Instant.now();
  }

  public String getOrderId() {
    return orderId;
  }

  public SagaState getState() {
    return state;
  }

  public void setState(SagaState state) {
    this.state = state;
  }

  public SagaState getLastSuccessfulState() {
    return lastSuccessfulState;
  }

  public void setLastSuccessfulState(SagaState lastSuccessfulState) {
    this.lastSuccessfulState = lastSuccessfulState;
  }

  public String getPendingCommand() {
    return pendingCommand;
  }

  public void setPendingCommand(String pendingCommand) {
    this.pendingCommand = pendingCommand;
  }

  public String getPendingCommandId() {
    return pendingCommandId;
  }

  public void setPendingCommandId(String pendingCommandId) {
    this.pendingCommandId = pendingCommandId;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }

  public Instant getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public void setLastUpdatedAt(Instant lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }
}
