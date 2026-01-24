package com.agro.checkout.infrastructure.http;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogClient {
  public record ValidationItem(String variantId, int quantity) {
  }

  public record ValidationRequest(List<ValidationItem> items) {
  }

  public record ValidationResult(String variantId, boolean valid, String reason, boolean available) {
  }

  private final RestTemplate restTemplate;
  private final CatalogClientProperties properties;

  public CatalogClient(RestTemplate restTemplate, CatalogClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public List<ValidationResult> validate(List<ValidationItem> items) {
    ResponseEntity<ValidationResult[]> response = restTemplate.exchange(
        properties.baseUrl() + "/catalog/validate",
        HttpMethod.POST,
        new HttpEntity<>(new ValidationRequest(items)),
        ValidationResult[].class);
    return response.getBody() == null ? List.of() : List.of(response.getBody());
  }
}
