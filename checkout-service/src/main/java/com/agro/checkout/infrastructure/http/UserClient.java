package com.agro.checkout.infrastructure.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {
  public record Address(String id, String userId, String line1, String city, String country, String postalCode) {
  }

  private final RestTemplate restTemplate;
  private final UserClientProperties properties;

  public UserClient(RestTemplate restTemplate, UserClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public Address getAddress(String userId, String addressId) {
    ResponseEntity<Address> response = restTemplate.exchange(
        properties.baseUrl() + "/users/" + userId + "/addresses/" + addressId,
        HttpMethod.GET,
        null,
        Address.class);
    return response.getBody();
  }
}
