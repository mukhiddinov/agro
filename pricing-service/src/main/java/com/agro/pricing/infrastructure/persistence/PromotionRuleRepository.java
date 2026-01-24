package com.agro.pricing.infrastructure.persistence;

import com.agro.pricing.domain.PromotionRule;
import com.agro.pricing.domain.PromotionTargetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRuleRepository extends JpaRepository<PromotionRule, String> {
  List<PromotionRule> findByActiveTrueAndAppliesToAndTargetIdIn(PromotionTargetType appliesTo,
      List<String> targetIds);
}
