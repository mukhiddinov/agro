package com.agro.pricing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotion_rules")
public class PromotionRule {
  @Id
  private String id;

  private String name;

  @Enumerated(EnumType.STRING)
  private DiscountType type;

  private BigDecimal value;

  @Enumerated(EnumType.STRING)
  private PromotionTargetType appliesTo;

  private String targetId;
  private int minQty;
  private boolean active;
  private Instant startsAt;
  private Instant endsAt;

  protected PromotionRule() {
  }

  public PromotionRule(String id, String name, DiscountType type, BigDecimal value,
      PromotionTargetType appliesTo, String targetId, int minQty, boolean active,
      Instant startsAt, Instant endsAt) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.value = value;
    this.appliesTo = appliesTo;
    this.targetId = targetId;
    this.minQty = minQty;
    this.active = active;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public DiscountType getType() {
    return type;
  }

  public BigDecimal getValue() {
    return value;
  }

  public PromotionTargetType getAppliesTo() {
    return appliesTo;
  }

  public String getTargetId() {
    return targetId;
  }

  public int getMinQty() {
    return minQty;
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
