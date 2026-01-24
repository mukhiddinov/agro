package com.agro.checkout.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user")
public record UserClientProperties(String baseUrl) {
}
