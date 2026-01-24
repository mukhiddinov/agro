package com.agro.payment.infrastructure.kafka;

import java.math.BigDecimal;

public record PaymentEvent(String orderId, String paymentId, BigDecimal amount, String status) {
}
