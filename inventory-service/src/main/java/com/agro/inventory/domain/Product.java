package com.agro.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
  @Id
  private String productId;

  private int availableStock;

  protected Product() {
  }

  public Product(String productId, int availableStock) {
    this.productId = productId;
    this.availableStock = availableStock;
  }

  public String getProductId() {
    return productId;
  }

  public int getAvailableStock() {
    return availableStock;
  }

  public void decrease(int quantity) {
    this.availableStock -= quantity;
  }

  public void increase(int quantity) {
    this.availableStock += quantity;
  }
}
