package com.agro.catalog.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
  @Id
  private String id;

  private String name;
  private String parentId;
  private String slug;
  private int sortOrder;
  private boolean active;
  private Instant createdAt;
  private Instant updatedAt;

  protected Category() {
  }

  public Category(String id, String name, String parentId, String slug, int sortOrder, boolean active,
      Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.parentId = parentId;
    this.slug = slug;
    this.sortOrder = sortOrder;
    this.active = active;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
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
