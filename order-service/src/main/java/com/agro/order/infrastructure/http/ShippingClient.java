package com.agro.order.infrastructure.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ShippingClient {
  public record ShipmentRequest(String orderId, String addressId, String shippingOptionId) {
  }

  public record ShipmentResponse(String shipmentId) {
  }

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public ShippingClient(RestTemplate restTemplate, @Value("${shipping.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public boolean createShipment(String orderId, String addressId, String shippingOptionId) {
    ResponseEntity<ShipmentResponse> response = restTemplate.exchange(
        baseUrl + "/shipping/shipments",
        HttpMethod.POST,
        new HttpEntity<>(new ShipmentRequest(orderId, addressId, shippingOptionId)),
        ShipmentResponse.class);
    return response.getStatusCode().is2xxSuccessful();
  }
}
