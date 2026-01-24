package com.agro.checkout.infrastructure.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderClient {
  public record OrderCreateRequest(
      String productId,
      int quantity,
      String buyerId,
      double amount,
      String currency,
      String paymentMethodId,
      String shippingOptionId,
      String addressId
  ) {
  }

  public record OrderResponse(String orderId) {
  }

  private final RestTemplate restTemplate;
  private final OrderClientProperties properties;

  public OrderClient(RestTemplate restTemplate, OrderClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public OrderResponse createOrder(OrderCreateRequest request) {
    ResponseEntity<OrderResponse> response = restTemplate.exchange(
        properties.baseUrl() + "/orders",
        HttpMethod.POST,
        new HttpEntity<>(request),
        OrderResponse.class);
    return response.getBody();
  }
}
