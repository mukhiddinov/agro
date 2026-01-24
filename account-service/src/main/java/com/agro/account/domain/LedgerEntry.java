package com.agro.account.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_ledger_entry_command", columnNames = "commandId")
})
public class LedgerEntry {
  @Id
  private String id;

  private String commandId;
  private String orderId;
  private String accountId;
  private double amount;

  @Enumerated(EnumType.STRING)
  private TransactionType type;

  private Instant createdAt;

  protected LedgerEntry() {
  }

  public LedgerEntry(String id, String commandId, String orderId, String accountId, double amount,
      TransactionType type, Instant createdAt) {
    this.id = id;
    this.commandId = commandId;
    this.orderId = orderId;
    this.accountId = accountId;
    this.amount = amount;
    this.type = type;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getCommandId() {
    return commandId;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getAccountId() {
    return accountId;
  }

  public double getAmount() {
    return amount;
  }

  public TransactionType getType() {
    return type;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
