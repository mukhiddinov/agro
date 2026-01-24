package com.agro.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {
  @Id
  private String id;

  private String orderId;
  private String buyerId;
  private String paymentMethodId;
  private BigDecimal amount;
  private String currency;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected Payment() {
  }

  public Payment(String id, String orderId, String buyerId, String paymentMethodId,
      BigDecimal amount, String currency, PaymentStatus status, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.orderId = orderId;
    this.buyerId = buyerId;
    this.paymentMethodId = paymentMethodId;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getBuyerId() {
    return buyerId;
  }

  public String getPaymentMethodId() {
    return paymentMethodId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
