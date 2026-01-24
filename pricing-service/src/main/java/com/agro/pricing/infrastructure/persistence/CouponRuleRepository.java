package com.agro.pricing.infrastructure.persistence;

import com.agro.pricing.domain.CouponRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRuleRepository extends JpaRepository<CouponRule, String> {
}
