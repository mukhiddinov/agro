package com.agro.checkout.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order")
public record OrderClientProperties(String baseUrl) {
}
