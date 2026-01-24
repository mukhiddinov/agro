package com.agro.pricing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tax_profiles")
public class TaxProfile {
  @Id
  private String variantId;

  @Enumerated(EnumType.STRING)
  private TaxClass taxClass;

  private boolean taxable;
  private Instant updatedAt;

  protected TaxProfile() {
  }

  public TaxProfile(String variantId, TaxClass taxClass, boolean taxable, Instant updatedAt) {
    this.variantId = variantId;
    this.taxClass = taxClass;
    this.taxable = taxable;
    this.updatedAt = updatedAt;
  }

  public String getVariantId() {
    return variantId;
  }

  public TaxClass getTaxClass() {
    return taxClass;
  }

  public boolean isTaxable() {
    return taxable;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
