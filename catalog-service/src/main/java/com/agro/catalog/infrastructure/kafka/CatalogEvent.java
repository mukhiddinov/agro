package com.agro.catalog.infrastructure.kafka;

public record CatalogEvent(String entityId, String entityType, String action) {
}
