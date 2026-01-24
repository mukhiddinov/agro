package com.agro.account.application;

import com.agro.account.domain.Account;
import com.agro.account.domain.LedgerEntry;
import com.agro.account.domain.ProcessedCommand;
import com.agro.account.domain.TransactionType;
import com.agro.account.infrastructure.kafka.AccountEvent;
import com.agro.account.infrastructure.kafka.AccountEventPublisher;
import com.agro.account.infrastructure.persistence.AccountRepository;
import com.agro.account.infrastructure.persistence.LedgerEntryRepository;
import com.agro.account.infrastructure.persistence.ProcessedCommandRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountApplicationService {
  public record AccountResult(boolean success, String message) {
  }

  private final AccountRepository accountRepository;
  private final LedgerEntryRepository ledgerRepository;
  private final ProcessedCommandRepository commandRepository;
  private final AccountEventPublisher eventPublisher;

  public AccountApplicationService(AccountRepository accountRepository,
      LedgerEntryRepository ledgerRepository,
      ProcessedCommandRepository commandRepository,
      AccountEventPublisher eventPublisher) {
    this.accountRepository = accountRepository;
    this.ledgerRepository = ledgerRepository;
    this.commandRepository = commandRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public AccountResult debitBuyer(String commandId, String orderId, String buyerAccountId, double amount) {
    if (commandRepository.existsById(commandId)) {
      return new AccountResult(true, "Command already processed");
    }

    Account account = accountRepository.findById(buyerAccountId).orElse(null);
    if (account == null || account.getBalance() < amount) {
      return new AccountResult(false, "Insufficient balance");
    }

    LedgerEntry entry = new LedgerEntry(UUID.randomUUID().toString(), commandId, orderId, buyerAccountId,
        amount, TransactionType.DEBIT, Instant.now());
    applyEntry(account, entry);
    commandRepository.save(new ProcessedCommand(commandId, "DEBIT"));

    registerEventAfterCommit(new AccountEvent(orderId, buyerAccountId, amount, "DEBITED"),
        AccountEventPublisher.TOPIC_BUYER_DEBITED);
    return new AccountResult(true, "Buyer debited");
  }

  @Transactional
  public AccountResult creditSeller(String commandId, String orderId, String sellerAccountId, double amount) {
    if (commandRepository.existsById(commandId)) {
      return new AccountResult(true, "Command already processed");
    }

    Account account = accountRepository.findById(sellerAccountId).orElse(null);
    if (account == null) {
      return new AccountResult(false, "Seller account not found");
    }

    LedgerEntry entry = new LedgerEntry(UUID.randomUUID().toString(), commandId, orderId, sellerAccountId,
        amount, TransactionType.CREDIT, Instant.now());
    applyEntry(account, entry);
    commandRepository.save(new ProcessedCommand(commandId, "CREDIT"));

    registerEventAfterCommit(new AccountEvent(orderId, sellerAccountId, amount, "CREDITED"),
        AccountEventPublisher.TOPIC_SELLER_CREDITED);
    return new AccountResult(true, "Seller credited");
  }

  @Transactional
  public AccountResult refundBuyer(String commandId, String orderId, String buyerAccountId) {
    if (commandRepository.existsById(commandId)) {
      return new AccountResult(true, "Command already processed");
    }

    Optional<LedgerEntry> original = ledgerRepository.findFirstByOrderIdAndAccountIdAndType(orderId,
        buyerAccountId, TransactionType.DEBIT);
    if (original.isEmpty()) {
      return new AccountResult(false, "Original debit not found");
    }

    Account account = accountRepository.findById(buyerAccountId).orElse(null);
    if (account == null) {
      return new AccountResult(false, "Buyer account not found");
    }

    double amount = original.get().getAmount();
    LedgerEntry entry = new LedgerEntry(UUID.randomUUID().toString(), commandId, orderId, buyerAccountId,
        amount, TransactionType.REFUND, Instant.now());
    applyEntry(account, entry);
    commandRepository.save(new ProcessedCommand(commandId, "REFUND"));

    registerEventAfterCommit(new AccountEvent(orderId, buyerAccountId, amount, "REFUNDED"),
        AccountEventPublisher.TOPIC_BUYER_REFUNDED);
    return new AccountResult(true, "Buyer refunded");
  }

  @Transactional
  public AccountResult reverseSellerCredit(String commandId, String orderId, String sellerAccountId) {
    if (commandRepository.existsById(commandId)) {
      return new AccountResult(true, "Command already processed");
    }

    Optional<LedgerEntry> original = ledgerRepository.findFirstByOrderIdAndAccountIdAndType(orderId,
        sellerAccountId, TransactionType.CREDIT);
    if (original.isEmpty()) {
      return new AccountResult(false, "Original credit not found");
    }

    Account account = accountRepository.findById(sellerAccountId).orElse(null);
    if (account == null || account.getBalance() < original.get().getAmount()) {
      return new AccountResult(false, "Unable to reverse seller credit");
    }

    double amount = original.get().getAmount();
    LedgerEntry entry = new LedgerEntry(UUID.randomUUID().toString(), commandId, orderId, sellerAccountId,
        amount, TransactionType.REVERSE, Instant.now());
    applyEntry(account, entry);
    commandRepository.save(new ProcessedCommand(commandId, "REVERSE"));

    registerEventAfterCommit(new AccountEvent(orderId, sellerAccountId, amount, "REVERSED"),
        AccountEventPublisher.TOPIC_SELLER_REVERSED);
    return new AccountResult(true, "Seller credit reversed");
  }

  private void applyEntry(Account account, LedgerEntry entry) {
    ledgerRepository.save(entry);
    switch (entry.getType()) {
      case DEBIT, REVERSE -> account.debit(entry.getAmount());
      case CREDIT, REFUND -> account.credit(entry.getAmount());
    }
    accountRepository.save(account);
  }

  private void registerEventAfterCommit(AccountEvent event, String topic) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventPublisher.publish(topic, event);
      }
    });
  }
}
