package com.agro.checkout.domain;

import java.math.BigDecimal;

public record ShippingOption(
    String optionId,
    String carrier,
    String serviceLevel,
    BigDecimal amount,
    String currency,
    String estimatedDelivery
) {
}
