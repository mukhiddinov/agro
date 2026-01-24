package com.agro.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stock_reservations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_reservation_order_product", columnNames = {"orderId", "productId"})
})
public class StockReservation {
  @Id
  private String id;

  private String orderId;
  private String productId;
  private int quantity;
  @Enumerated(EnumType.STRING)
  private ReservationStatus status;

  protected StockReservation() {
  }

  public StockReservation(String id, String orderId, String productId, int quantity, ReservationStatus status) {
    this.id = id;
    this.orderId = orderId;
    this.productId = productId;
    this.quantity = quantity;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getProductId() {
    return productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus status) {
    this.status = status;
  }
}
