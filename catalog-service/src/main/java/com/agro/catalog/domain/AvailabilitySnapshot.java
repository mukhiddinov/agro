package com.agro.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "availability_snapshots")
public class AvailabilitySnapshot {
  @Id
  private String variantId;

  private boolean available;
  private Instant lastSyncAt;

  protected AvailabilitySnapshot() {
  }

  public AvailabilitySnapshot(String variantId, boolean available, Instant lastSyncAt) {
    this.variantId = variantId;
    this.available = available;
    this.lastSyncAt = lastSyncAt;
  }

  public String getVariantId() {
    return variantId;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public Instant getLastSyncAt() {
    return lastSyncAt;
  }

  public void setLastSyncAt(Instant lastSyncAt) {
    this.lastSyncAt = lastSyncAt;
  }
}
