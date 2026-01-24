package com.agro.catalog.infrastructure.persistence;

import com.agro.catalog.domain.AvailabilitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySnapshotRepository extends JpaRepository<AvailabilitySnapshot, String> {
}
