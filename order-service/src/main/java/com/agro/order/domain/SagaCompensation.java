package com.agro.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "saga_compensations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_saga_compensation_order_action", columnNames = {"orderId", "action"})
})
public class SagaCompensation {
  @Id
  private String id;

  private String orderId;
  private String action;

  protected SagaCompensation() {
  }

  public SagaCompensation(String id, String orderId, String action) {
    this.id = id;
    this.orderId = orderId;
    this.action = action;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getAction() {
    return action;
  }
}
