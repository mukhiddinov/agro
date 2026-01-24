package com.agro.order.infrastructure.kafka;

public record SagaEvent(String orderId, String step, String status, String message) {
}
