package com.agro.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_commands")
public class ProcessedCommand {
  @Id
  private String commandId;

  private String action;

  protected ProcessedCommand() {
  }

  public ProcessedCommand(String commandId, String action) {
    this.commandId = commandId;
    this.action = action;
  }

  public String getCommandId() {
    return commandId;
  }

  public String getAction() {
    return action;
  }
}
