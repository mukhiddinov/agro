package com.agro.pricing.infrastructure.persistence;

import com.agro.pricing.domain.BasePrice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasePriceRepository extends JpaRepository<BasePrice, String> {
  List<BasePrice> findByVariantIdInAndCurrency(List<String> variantIds, String currency);
}
