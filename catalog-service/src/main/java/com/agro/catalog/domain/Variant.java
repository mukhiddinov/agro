package com.agro.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "variants")
public class Variant {
  @Id
  private String id;

  private String productId;
  private String sku;

  @Column(columnDefinition = "text")
  private String attributesJson;

  @Enumerated(EnumType.STRING)
  private VariantStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected Variant() {
  }

  public Variant(String id, String productId, String sku, String attributesJson, VariantStatus status,
      Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.productId = productId;
    this.sku = sku;
    this.attributesJson = attributesJson;
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

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getAttributesJson() {
    return attributesJson;
  }

  public void setAttributesJson(String attributesJson) {
    this.attributesJson = attributesJson;
  }

  public VariantStatus getStatus() {
    return status;
  }

  public void setStatus(VariantStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
