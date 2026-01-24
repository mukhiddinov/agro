package com.agro.cart.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "carts")
public class Cart {
  @Id
  private String id;

  private String userId;

  @Enumerated(EnumType.STRING)
  private CartStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected Cart() {
  }

  public Cart(String id, String userId, CartStatus status, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public CartStatus getStatus() {
    return status;
  }

  public void setStatus(CartStatus status) {
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
