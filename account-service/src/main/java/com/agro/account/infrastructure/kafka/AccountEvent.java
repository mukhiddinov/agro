package com.agro.account.infrastructure.kafka;

public record AccountEvent(String orderId, String accountId, double amount, String status) {
}
