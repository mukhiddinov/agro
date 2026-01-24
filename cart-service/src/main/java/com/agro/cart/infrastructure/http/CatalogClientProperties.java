package com.agro.cart.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog")
public record CatalogClientProperties(String baseUrl) {
}
