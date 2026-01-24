package com.agro.inventory.infrastructure.persistence;

import com.agro.inventory.domain.ProcessedCommand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCommandRepository extends JpaRepository<ProcessedCommand, String> {
}
