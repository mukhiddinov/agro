package com.agro.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  private String id;

  private String productId;
  private int quantity;
  private String buyerId;
  private double amount;
  private String currency;
  private String paymentMethodId;
  private String shippingOptionId;
  private String addressId;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected Order() {
  }

  public Order(String id, String productId, int quantity, String buyerId, double amount,
      String currency, String paymentMethodId, String shippingOptionId, String addressId,
      OrderStatus status, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.productId = productId;
    this.quantity = quantity;
    this.buyerId = buyerId;
    this.amount = amount;
    this.currency = currency;
    this.paymentMethodId = paymentMethodId;
    this.shippingOptionId = shippingOptionId;
    this.addressId = addressId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getProductId() {
    return productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public String getBuyerId() {
    return buyerId;
  }

  public double getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getPaymentMethodId() {
    return paymentMethodId;
  }

  public String getShippingOptionId() {
    return shippingOptionId;
  }

  public String getAddressId() {
    return addressId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
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
