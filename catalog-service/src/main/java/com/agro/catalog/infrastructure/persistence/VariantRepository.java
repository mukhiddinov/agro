package com.agro.catalog.infrastructure.persistence;

import com.agro.catalog.domain.Variant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, String> {
  List<Variant> findByProductId(String productId);
}
