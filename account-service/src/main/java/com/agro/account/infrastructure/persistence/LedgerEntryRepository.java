package com.agro.account.infrastructure.persistence;

import com.agro.account.domain.LedgerEntry;
import com.agro.account.domain.TransactionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
  Optional<LedgerEntry> findByCommandId(String commandId);
  Optional<LedgerEntry> findFirstByOrderIdAndAccountIdAndType(String orderId, String accountId,
      TransactionType type);
}
