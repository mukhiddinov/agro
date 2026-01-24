package com.agro.inventory.infrastructure.kafka;

public record InventoryEvent(String orderId, String productId, int quantity, String status) {
}
