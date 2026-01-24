package com.agro.order.application;

public record PlaceOrderCommand(
    String productId,
    int quantity,
    String buyerId,
    double amount,
    String currency,
    String paymentMethodId,
    String shippingOptionId,
    String addressId
) {
}
