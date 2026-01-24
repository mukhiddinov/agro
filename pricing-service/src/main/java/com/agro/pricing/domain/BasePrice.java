package com.agro.pricing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "base_prices", uniqueConstraints = {
    @UniqueConstraint(name = "uk_base_price_variant_currency", columnNames = {"variantId", "currency"})
})
public class BasePrice {
  @Id
  private String id;

  private String variantId;
  private String currency;
  private BigDecimal amount;
  private Instant updatedAt;

  protected BasePrice() {
  }

  public BasePrice(String id, String variantId, String currency, BigDecimal amount, Instant updatedAt) {
    this.id = id;
    this.variantId = variantId;
    this.currency = currency;
    this.amount = amount;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getVariantId() {
    return variantId;
  }

  public String getCurrency() {
    return currency;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
