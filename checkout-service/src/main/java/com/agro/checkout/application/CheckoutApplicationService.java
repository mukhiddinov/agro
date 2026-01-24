package com.agro.checkout.application;

import com.agro.checkout.domain.OrderIntent;
import com.agro.checkout.domain.ShippingOption;
import com.agro.checkout.infrastructure.http.CartClient;
import com.agro.checkout.infrastructure.http.CatalogClient;
import com.agro.checkout.infrastructure.http.PricingClient;
import com.agro.checkout.infrastructure.http.ShippingClient;
import com.agro.checkout.infrastructure.http.UserClient;
import com.agro.checkout.infrastructure.http.OrderClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CheckoutApplicationService {
  public record CheckoutResult(String orderId) {
  }

  public record CheckoutRequest(
      String cartId,
      String userId,
      String addressId,
      String currency,
      String couponCode,
      String shippingOptionId,
      String paymentMethodId
  ) {
  }

  private final CartClient cartClient;
  private final CatalogClient catalogClient;
  private final PricingClient pricingClient;
  private final ShippingClient shippingClient;
  private final UserClient userClient;
  private final OrderClient orderClient;

  public CheckoutApplicationService(CartClient cartClient,
      CatalogClient catalogClient,
      PricingClient pricingClient,
      ShippingClient shippingClient,
      UserClient userClient,
      OrderClient orderClient) {
    this.cartClient = cartClient;
    this.catalogClient = catalogClient;
    this.pricingClient = pricingClient;
    this.shippingClient = shippingClient;
    this.userClient = userClient;
    this.orderClient = orderClient;
  }

  public CheckoutResult checkout(CheckoutRequest request) {
    CartClient.CartView cartView = cartClient.getCart(request.cartId());
    if (cartView == null || cartView.cart() == null) {
      throw new IllegalArgumentException("Cart not found");
    }
    if (!cartView.cart().userId().equals(request.userId())) {
      throw new IllegalArgumentException("Cart user mismatch");
    }
    if (cartView.items() == null || cartView.items().isEmpty()) {
      throw new IllegalArgumentException("Cart is empty");
    }
    if (cartView.items().size() != 1) {
      throw new IllegalArgumentException("Only single-item carts are supported");
    }

    UserClient.Address address = userClient.getAddress(request.userId(), request.addressId());
    if (address == null) {
      throw new IllegalArgumentException("Address not found");
    }

    List<CatalogClient.ValidationItem> validationItems = cartView.items().stream()
        .map(item -> new CatalogClient.ValidationItem(item.variantId(), item.quantity()))
        .toList();
    List<CatalogClient.ValidationResult> validation = catalogClient.validate(validationItems);
    if (validation.stream().anyMatch(result -> !result.valid())) {
      throw new IllegalArgumentException("Cart items not purchasable");
    }

    List<PricingClient.QuoteItem> quoteItems = cartView.items().stream()
        .map(item -> new PricingClient.QuoteItem(item.variantId(), item.quantity(), item.categoryId()))
        .toList();
    PricingClient.QuoteResponse pricing = pricingClient.quote(request.currency(), request.couponCode(),
        quoteItems);
    if (pricing == null) {
      throw new IllegalStateException("Pricing unavailable");
    }

    ShippingClient.ShippingAddress shippingAddress = new ShippingClient.ShippingAddress(
        address.line1(), address.city(), address.country(), address.postalCode());
    List<ShippingClient.ShippingItem> shippingItems = cartView.items().stream()
        .map(item -> new ShippingClient.ShippingItem(item.variantId(), item.quantity()))
        .toList();
    List<ShippingClient.ShippingOption> options = shippingClient.options(
        new ShippingClient.ShippingRequest(shippingAddress, shippingItems, pricing.currency()));

    ShippingClient.ShippingOption selected = options.stream()
        .filter(option -> option.optionId().equals(request.shippingOptionId()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid shipping option"));

    BigDecimal subtotal = pricing.subtotal();
    BigDecimal shippingAmount = selected.amount();
    BigDecimal totalPayable = subtotal.add(shippingAmount).max(BigDecimal.ZERO);

    List<OrderIntent.OrderIntentItem> items = pricing.items().stream()
        .map(item -> new OrderIntent.OrderIntentItem(item.variantId(), item.quantity(),
            item.baseUnitPrice(), item.discountUnitPrice(), item.finalUnitPrice(), item.lineTotal(),
            item.taxClass(), item.taxable()))
        .toList();

    OrderIntent intent = new OrderIntent(
        UUID.randomUUID().toString(),
        request.cartId(),
        request.userId(),
        pricing.currency(),
        items,
        new ShippingOption(selected.optionId(), selected.carrier(), selected.serviceLevel(),
            selected.amount(), selected.currency(), selected.estimatedDelivery()),
        pricing.subtotal(),
        pricing.discountTotal(),
        pricing.taxableAmount(),
        shippingAmount,
        totalPayable,
        Instant.now(),
        Instant.now().plusSeconds(900));

    OrderClient.OrderCreateRequest orderRequest = new OrderClient.OrderCreateRequest(
        intent.items().get(0).variantId(),
        intent.items().get(0).quantity(),
        intent.userId(),
        intent.totalPayable().doubleValue(),
        intent.currency(),
        request.paymentMethodId(),
        intent.shippingOption().optionId(),
        request.addressId());

    OrderClient.OrderResponse orderResponse = orderClient.createOrder(orderRequest);
    if (orderResponse == null) {
      throw new IllegalStateException("Order service unavailable");
    }
    return new CheckoutResult(orderResponse.orderId());
  }
}
