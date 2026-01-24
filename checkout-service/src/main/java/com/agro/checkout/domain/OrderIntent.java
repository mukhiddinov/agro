package com.agro.checkout.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderIntent(
    String intentId,
    String cartId,
    String userId,
    String currency,
    List<OrderIntentItem> items,
    ShippingOption shippingOption,
    BigDecimal subtotal,
    BigDecimal discountTotal,
    BigDecimal taxableAmount,
    BigDecimal shippingAmount,
    BigDecimal totalPayable,
    Instant createdAt,
    Instant expiresAt
) {
  public record OrderIntentItem(
      String variantId,
      int quantity,
      BigDecimal baseUnitPrice,
      BigDecimal discountUnitPrice,
      BigDecimal finalUnitPrice,
      BigDecimal lineTotal,
      String taxClass,
      boolean taxable
  ) {
  }
}
