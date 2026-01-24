package com.agro.account.infrastructure.persistence;

import com.agro.account.domain.ProcessedCommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCommandRepository extends JpaRepository<ProcessedCommand, String> {
}
