package com.agro.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "addresses")
public class Address {
  @Id
  private String id;

  private String userId;
  private String line1;
  private String city;
  private String country;
  private String postalCode;
  private Instant createdAt;
  private Instant updatedAt;

  protected Address() {
  }

  public Address(String id, String userId, String line1, String city, String country,
      String postalCode, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.line1 = line1;
    this.city = city;
    this.country = country;
    this.postalCode = postalCode;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getLine1() {
    return line1;
  }

  public String getCity() {
    return city;
  }

  public String getCountry() {
    return country;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
