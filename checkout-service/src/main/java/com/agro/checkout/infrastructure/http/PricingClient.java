package com.agro.checkout.infrastructure.http;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PricingClient {
  public record QuoteItem(String variantId, int quantity, String categoryId) {
  }

  public record QuoteRequest(String currency, String couponCode, List<QuoteItem> items) {
  }

  public record QuoteItemResult(
      String variantId,
      int quantity,
      BigDecimal baseUnitPrice,
      BigDecimal discountUnitPrice,
      BigDecimal finalUnitPrice,
      BigDecimal lineTotal,
      String taxClass,
      boolean taxable
  ) {
  }

  public record QuoteResponse(
      String pricingVersion,
      String currency,
      List<QuoteItemResult> items,
      BigDecimal subtotal,
      BigDecimal discountTotal,
      BigDecimal taxableAmount
  ) {
  }

  private final RestTemplate restTemplate;
  private final PricingClientProperties properties;

  public PricingClient(RestTemplate restTemplate, PricingClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public QuoteResponse quote(String currency, String couponCode, List<QuoteItem> items) {
    ResponseEntity<QuoteResponse> response = restTemplate.exchange(
        properties.baseUrl() + "/pricing/quote",
        HttpMethod.POST,
        new HttpEntity<>(new QuoteRequest(currency, couponCode, items)),
        QuoteResponse.class);
    return response.getBody();
  }
}
