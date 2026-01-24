package com.agro.checkout.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shipping")
public record ShippingClientProperties(String baseUrl) {
}
