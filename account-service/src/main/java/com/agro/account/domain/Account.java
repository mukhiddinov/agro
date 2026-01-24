package com.agro.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {
  @Id
  private String id;

  private String ownerId;

  @Enumerated(EnumType.STRING)
  private AccountType type;

  private double balance;

  protected Account() {
  }

  public Account(String id, String ownerId, AccountType type, double balance) {
    this.id = id;
    this.ownerId = ownerId;
    this.type = type;
    this.balance = balance;
  }

  public String getId() {
    return id;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public AccountType getType() {
    return type;
  }

  public double getBalance() {
    return balance;
  }

  public void debit(double amount) {
    this.balance -= amount;
  }

  public void credit(double amount) {
    this.balance += amount;
  }
}
