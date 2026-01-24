package com.agro.pricing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupon_rules")
public class CouponRule {
  @Id
  private String code;

  @Enumerated(EnumType.STRING)
  private DiscountType type;

  private BigDecimal value;
  private boolean active;
  private Instant startsAt;
  private Instant endsAt;

  protected CouponRule() {
  }

  public CouponRule(String code, DiscountType type, BigDecimal value, boolean active,
      Instant startsAt, Instant endsAt) {
    this.code = code;
    this.type = type;
    this.value = value;
    this.active = active;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  public String getCode() {
    return code;
  }

  public DiscountType getType() {
    return type;
  }

  public BigDecimal getValue() {
    return value;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public Instant getEndsAt() {
    return endsAt;
  }
}
