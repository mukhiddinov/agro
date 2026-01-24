package com.agro.pricing.infrastructure.persistence;

import com.agro.pricing.domain.TaxProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxProfileRepository extends JpaRepository<TaxProfile, String> {
  List<TaxProfile> findByVariantIdIn(List<String> variantIds);
}
