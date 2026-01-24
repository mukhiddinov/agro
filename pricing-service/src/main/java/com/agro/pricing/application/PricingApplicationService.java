package com.agro.pricing.application;

import com.agro.pricing.domain.BasePrice;
import com.agro.pricing.domain.CouponRule;
import com.agro.pricing.domain.DiscountType;
import com.agro.pricing.domain.PromotionRule;
import com.agro.pricing.domain.PromotionTargetType;
import com.agro.pricing.domain.TaxClass;
import com.agro.pricing.domain.TaxProfile;
import com.agro.pricing.infrastructure.persistence.BasePriceRepository;
import com.agro.pricing.infrastructure.persistence.CouponRuleRepository;
import com.agro.pricing.infrastructure.persistence.PromotionRuleRepository;
import com.agro.pricing.infrastructure.persistence.TaxProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingApplicationService {
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  public record PricingItemRequest(String variantId, int quantity, String categoryId) {
  }

  public record PricingRequest(String currency, String couponCode, List<PricingItemRequest> items) {
  }

  public record PricingItemResult(
      String variantId,
      int quantity,
      BigDecimal baseUnitPrice,
      BigDecimal discountUnitPrice,
      BigDecimal finalUnitPrice,
      BigDecimal lineTotal,
      TaxClass taxClass,
      boolean taxable
  ) {
  }

  public record PricingResult(
      String pricingVersion,
      String currency,
      List<PricingItemResult> items,
      BigDecimal subtotal,
      BigDecimal discountTotal,
      BigDecimal taxableAmount
  ) {
  }

  private final BasePriceRepository basePriceRepository;
  private final PromotionRuleRepository promotionRuleRepository;
  private final TaxProfileRepository taxProfileRepository;
  private final CouponRuleRepository couponRuleRepository;

  public PricingApplicationService(BasePriceRepository basePriceRepository,
      PromotionRuleRepository promotionRuleRepository,
      TaxProfileRepository taxProfileRepository,
      CouponRuleRepository couponRuleRepository) {
    this.basePriceRepository = basePriceRepository;
    this.promotionRuleRepository = promotionRuleRepository;
    this.taxProfileRepository = taxProfileRepository;
    this.couponRuleRepository = couponRuleRepository;
  }

  @Transactional(readOnly = true)
  public PricingResult quote(PricingRequest request) {
    List<String> variantIds = request.items().stream().map(PricingItemRequest::variantId).toList();
    Map<String, BasePrice> basePrices = basePriceRepository.findByVariantIdInAndCurrency(
        variantIds, request.currency()).stream()
        .collect(java.util.stream.Collectors.toMap(BasePrice::getVariantId, price -> price));

    Map<String, TaxProfile> taxProfiles = taxProfileRepository.findByVariantIdIn(variantIds).stream()
        .collect(java.util.stream.Collectors.toMap(TaxProfile::getVariantId, profile -> profile));

    List<PromotionRule> variantPromotions = promotionRuleRepository
        .findByActiveTrueAndAppliesToAndTargetIdIn(PromotionTargetType.VARIANT, variantIds);

    List<String> categoryIds = request.items().stream()
        .map(PricingItemRequest::categoryId)
        .filter(id -> id != null && !id.isBlank())
        .distinct()
        .toList();

    List<PromotionRule> categoryPromotions = categoryIds.isEmpty()
        ? List.of()
        : promotionRuleRepository
            .findByActiveTrueAndAppliesToAndTargetIdIn(PromotionTargetType.CATEGORY, categoryIds);

    Instant now = Instant.now();
    List<PricingItemResult> results = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal discountTotal = BigDecimal.ZERO;
    BigDecimal taxableAmount = BigDecimal.ZERO;

    for (PricingItemRequest item : request.items()) {
      BasePrice basePrice = basePrices.get(item.variantId());
      if (basePrice == null) {
        continue;
      }

      BigDecimal baseUnit = basePrice.getAmount();
      BigDecimal discountUnit = applyPromotion(baseUnit, item.quantity(), item.variantId(),
          item.categoryId(), variantPromotions, categoryPromotions, now);
      BigDecimal finalUnit = baseUnit.subtract(discountUnit).max(BigDecimal.ZERO);
      BigDecimal lineTotal = finalUnit.multiply(BigDecimal.valueOf(item.quantity()));

      TaxProfile taxProfile = taxProfiles.get(item.variantId());
      TaxClass taxClass = taxProfile != null ? taxProfile.getTaxClass() : TaxClass.STANDARD;
      boolean taxable = taxProfile == null || taxProfile.isTaxable();

      results.add(new PricingItemResult(item.variantId(), item.quantity(), baseUnit, discountUnit,
          finalUnit, lineTotal, taxClass, taxable));

      subtotal = subtotal.add(lineTotal);
      discountTotal = discountTotal.add(discountUnit.multiply(BigDecimal.valueOf(item.quantity())));
      if (taxable) {
        taxableAmount = taxableAmount.add(lineTotal);
      }
    }

    Optional<CouponRule> coupon = resolveCoupon(request.couponCode(), now);
    if (coupon.isPresent()) {
      BigDecimal couponDiscount = computeCouponDiscount(coupon.get(), subtotal, results);
      subtotal = subtotal.subtract(couponDiscount).max(BigDecimal.ZERO);
      discountTotal = discountTotal.add(couponDiscount);
    }

    String pricingVersion = String.valueOf(System.currentTimeMillis());
    return new PricingResult(pricingVersion, request.currency(), results,
        scale(subtotal), scale(discountTotal), scale(taxableAmount));
  }

  private BigDecimal applyPromotion(BigDecimal baseUnit, int quantity, String variantId, String categoryId,
      List<PromotionRule> variantPromotions, List<PromotionRule> categoryPromotions, Instant now) {
    List<PromotionRule> applicable = new ArrayList<>();
    for (PromotionRule rule : variantPromotions) {
      if (rule.getTargetId().equals(variantId) && rule.getMinQty() <= quantity && isActive(rule, now)) {
        applicable.add(rule);
      }
    }
    for (PromotionRule rule : categoryPromotions) {
      if (categoryId != null && rule.getTargetId().equals(categoryId)
          && rule.getMinQty() <= quantity && isActive(rule, now)) {
        applicable.add(rule);
      }
    }

    if (applicable.isEmpty()) {
      return BigDecimal.ZERO;
    }

    PromotionRule bestRule = applicable.stream()
        .max(Comparator.comparing(rule -> discountValue(rule, baseUnit)))
        .orElse(applicable.get(0));
    return discountValue(bestRule, baseUnit);
  }

  private BigDecimal discountValue(PromotionRule rule, BigDecimal baseUnit) {
    if (rule.getType() == DiscountType.PERCENT) {
      return scale(baseUnit.multiply(rule.getValue()).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
    }
    return scale(rule.getValue());
  }

  private Optional<CouponRule> resolveCoupon(String code, Instant now) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    return couponRuleRepository.findById(code)
        .filter(rule -> rule.isActive())
        .filter(rule -> rule.getStartsAt() == null || !now.isBefore(rule.getStartsAt()))
        .filter(rule -> rule.getEndsAt() == null || !now.isAfter(rule.getEndsAt()));
  }

  private BigDecimal computeCouponDiscount(CouponRule rule, BigDecimal subtotal,
      List<PricingItemResult> results) {
    if (rule.getType() == DiscountType.PERCENT) {
      return scale(subtotal.multiply(rule.getValue()).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
    }

    BigDecimal flat = rule.getValue();
    BigDecimal total = results.stream().map(PricingItemResult::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (total.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal applied = BigDecimal.ZERO;
    for (PricingItemResult item : results) {
      BigDecimal ratio = item.lineTotal().divide(total, 4, RoundingMode.HALF_UP);
      BigDecimal itemDiscount = flat.multiply(ratio);
      applied = applied.add(itemDiscount);
    }
    return scale(applied.min(flat));
  }

  private boolean isActive(PromotionRule rule, Instant now) {
    boolean afterStart = rule.getStartsAt() == null || !now.isBefore(rule.getStartsAt());
    boolean beforeEnd = rule.getEndsAt() == null || !now.isAfter(rule.getEndsAt());
    return rule.isActive() && afterStart && beforeEnd;
  }

  private BigDecimal scale(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
