package com.agro.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
  @Id
  private String id;

  private String sku;
  private String name;
  private String description;
  private String brand;
  private String primaryCategoryId;

  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected Product() {
  }

  public Product(String id, String sku, String name, String description, String brand,
      String primaryCategoryId, ProductStatus status, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.brand = brand;
    this.primaryCategoryId = primaryCategoryId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getPrimaryCategoryId() {
    return primaryCategoryId;
  }

  public void setPrimaryCategoryId(String primaryCategoryId) {
    this.primaryCategoryId = primaryCategoryId;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public void setStatus(ProductStatus status) {
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
