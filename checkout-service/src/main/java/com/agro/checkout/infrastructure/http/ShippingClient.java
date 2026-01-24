package com.agro.checkout.infrastructure.http;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ShippingClient {
  public record ShippingAddress(String line1, String city, String country, String postalCode) {
  }

  public record ShippingItem(String variantId, int quantity) {
  }

  public record ShippingOption(
      String optionId,
      String carrier,
      String serviceLevel,
      BigDecimal amount,
      String currency,
      String estimatedDelivery
  ) {
  }

  public record ShippingRequest(ShippingAddress address, List<ShippingItem> items, String currency) {
  }

  private final RestTemplate restTemplate;
  private final ShippingClientProperties properties;

  public ShippingClient(RestTemplate restTemplate, ShippingClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public List<ShippingOption> options(ShippingRequest request) {
    ResponseEntity<ShippingOption[]> response = restTemplate.exchange(
        properties.baseUrl() + "/shipping/options",
        HttpMethod.POST,
        new HttpEntity<>(request),
        ShippingOption[].class);
    return response.getBody() == null ? List.of() : List.of(response.getBody());
  }
}
