package com.agro.order.infrastructure.kafka;

public record InventoryEvent(String orderId, String productId, int quantity, String status) {
}
