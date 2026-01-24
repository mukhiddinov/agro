package com.agro.order.infrastructure.kafka;

public record AccountEvent(String orderId, String accountId, double amount, String status) {
}
