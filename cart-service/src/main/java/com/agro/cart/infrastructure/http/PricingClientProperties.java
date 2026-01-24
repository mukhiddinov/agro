package com.agro.cart.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pricing")
public record PricingClientProperties(String baseUrl) {
}
