package com.agro.order.infrastructure.kafka;

import java.math.BigDecimal;

public record PaymentEvent(String orderId, String paymentId, BigDecimal amount, String status) {
}
