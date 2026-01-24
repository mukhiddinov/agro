package com.agro.checkout.infrastructure.http;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CartClient {
  public record Cart(String id, String userId, String status) {
  }

  public record CartItem(
      String id,
      String cartId,
      String variantId,
      String categoryId,
      int quantity,
      String currency,
      String pricingVersion,
      java.math.BigDecimal baseUnitPrice,
      java.math.BigDecimal discountUnitPrice,
      java.math.BigDecimal finalUnitPrice,
      java.math.BigDecimal lineTotal,
      String taxClass,
      boolean taxable
  ) {
  }

  public record CartView(Cart cart, List<CartItem> items) {
  }

  private final RestTemplate restTemplate;
  private final CartClientProperties properties;

  public CartClient(RestTemplate restTemplate, CartClientProperties properties) {
    this.restTemplate = restTemplate;
    this.properties = properties;
  }

  public CartView getCart(String cartId) {
    ResponseEntity<CartView> response = restTemplate.exchange(
        properties.baseUrl() + "/carts/" + cartId,
        HttpMethod.GET,
        null,
        CartView.class);
    return response.getBody();
  }
}
