package com.agro.cart.application;

import com.agro.cart.domain.Cart;
import com.agro.cart.domain.CartItem;
import com.agro.cart.domain.CartStatus;
import com.agro.cart.infrastructure.http.CatalogClient;
import com.agro.cart.infrastructure.http.PricingClient;
import com.agro.cart.infrastructure.persistence.CartItemRepository;
import com.agro.cart.infrastructure.persistence.CartRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartApplicationService {
  public record CartItemRequest(String variantId, String categoryId, int quantity) {
  }

  public record CartPricingSnapshot(
      String currency,
      String pricingVersion,
      List<PricingClient.QuoteItemResult> items,
      java.math.BigDecimal subtotal,
      java.math.BigDecimal discountTotal,
      java.math.BigDecimal taxableAmount
  ) {
  }

  private final CartRepository cartRepository;
  private final CartItemRepository itemRepository;
  private final CatalogClient catalogClient;
  private final PricingClient pricingClient;

  public CartApplicationService(CartRepository cartRepository,
      CartItemRepository itemRepository,
      CatalogClient catalogClient,
      PricingClient pricingClient) {
    this.cartRepository = cartRepository;
    this.itemRepository = itemRepository;
    this.catalogClient = catalogClient;
    this.pricingClient = pricingClient;
  }

  @Transactional
  public Cart createCart(String userId) {
    Instant now = Instant.now();
    Cart cart = new Cart(UUID.randomUUID().toString(), userId, CartStatus.ACTIVE, now, now);
    return cartRepository.save(cart);
  }

  public Cart getCart(String cartId) {
    return cartRepository.findById(cartId).orElse(null);
  }

  public List<CartItem> getItems(String cartId) {
    return itemRepository.findByCartId(cartId);
  }

  @Transactional
  public CartPricingSnapshot upsertItems(String cartId, String currency, String couponCode,
      List<CartItemRequest> items) {
    Cart cart = cartRepository.findById(cartId).orElse(null);
    if (cart == null) {
      return null;
    }

    List<CatalogClient.ValidationItem> validateItems = items.stream()
        .map(item -> new CatalogClient.ValidationItem(item.variantId(), item.quantity()))
        .toList();
    List<CatalogClient.ValidationResult> validation = catalogClient.validate(validateItems);
    if (validation.stream().anyMatch(result -> !result.valid())) {
      throw new IllegalArgumentException("Invalid cart items");
    }

    List<PricingClient.QuoteItem> quoteItems = items.stream()
        .map(item -> new PricingClient.QuoteItem(item.variantId(), item.quantity(), item.categoryId()))
        .toList();
    PricingClient.QuoteResponse quote = pricingClient.quote(currency, couponCode, quoteItems);
    if (quote == null) {
      throw new IllegalStateException("Pricing service unavailable");
    }

    Map<String, PricingClient.QuoteItemResult> priceMap = new HashMap<>();
    for (PricingClient.QuoteItemResult result : quote.items()) {
      priceMap.put(result.variantId(), result);
    }

    for (CartItemRequest item : items) {
      PricingClient.QuoteItemResult pricing = priceMap.get(item.variantId());
      if (pricing == null) {
        continue;
      }

      CartItem cartItem = itemRepository.findByCartIdAndVariantId(cartId, item.variantId())
          .orElseGet(() -> new CartItem(UUID.randomUUID().toString(), cartId, item.variantId(),
              item.categoryId(), item.quantity(), currency, quote.pricingVersion(),
              pricing.baseUnitPrice(), pricing.discountUnitPrice(), pricing.finalUnitPrice(),
              pricing.lineTotal(), pricing.taxClass(), pricing.taxable()));

      cartItem.setCategoryId(item.categoryId());
      cartItem.setQuantity(item.quantity());
      cartItem.setCurrency(quote.currency());
      cartItem.setPricingVersion(quote.pricingVersion());
      cartItem.setBaseUnitPrice(pricing.baseUnitPrice());
      cartItem.setDiscountUnitPrice(pricing.discountUnitPrice());
      cartItem.setFinalUnitPrice(pricing.finalUnitPrice());
      cartItem.setLineTotal(pricing.lineTotal());
      cartItem.setTaxClass(pricing.taxClass());
      cartItem.setTaxable(pricing.taxable());
      itemRepository.save(cartItem);
    }

    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);

    return new CartPricingSnapshot(quote.currency(), quote.pricingVersion(), quote.items(),
        quote.subtotal(), quote.discountTotal(), quote.taxableAmount());
  }

  @Transactional
  public boolean removeItem(String cartId, String variantId) {
    Cart cart = cartRepository.findById(cartId).orElse(null);
    if (cart == null) {
      return false;
    }
    itemRepository.deleteByCartIdAndVariantId(cartId, variantId);
    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);
    return true;
  }

  @Transactional
  public boolean deleteCart(String cartId) {
    Cart cart = cartRepository.findById(cartId).orElse(null);
    if (cart == null) {
      return false;
    }
    itemRepository.deleteAll(itemRepository.findByCartId(cartId));
    cartRepository.delete(cart);
    return true;
  }
}
