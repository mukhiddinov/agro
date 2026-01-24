package com.agro.shipping.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "shipments")
public class Shipment {
  @Id
  private String id;

  private String orderId;
  private String addressId;
  private String shippingOptionId;

  @Enumerated(EnumType.STRING)
  private ShipmentStatus status;

  private Instant createdAt;

  protected Shipment() {
  }

  public Shipment(String id, String orderId, String addressId, String shippingOptionId,
      ShipmentStatus status, Instant createdAt) {
    this.id = id;
    this.orderId = orderId;
    this.addressId = addressId;
    this.shippingOptionId = shippingOptionId;
    this.status = status;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getAddressId() {
    return addressId;
  }

  public String getShippingOptionId() {
    return shippingOptionId;
  }

  public ShipmentStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
