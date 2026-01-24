package com.agro.cart.interfaces.rest;

import com.agro.cart.application.CartApplicationService;
import com.agro.cart.application.CartApplicationService.CartItemRequest;
import com.agro.cart.application.CartApplicationService.CartPricingSnapshot;
import com.agro.cart.domain.Cart;
import com.agro.cart.domain.CartItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/carts")
public class CartController {
  private final CartApplicationService cartService;

  public CartController(CartApplicationService cartService) {
    this.cartService = cartService;
  }

  @PostMapping
  public ResponseEntity<Cart> create(@Valid @RequestBody CreateCartRequest request) {
    return ResponseEntity.ok(cartService.createCart(request.userId()));
  }

  @GetMapping("/{cartId}")
  public ResponseEntity<CartView> get(@PathVariable("cartId") String cartId) {
    Cart cart = cartService.getCart(cartId);
    if (cart == null) {
      return ResponseEntity.notFound().build();
    }
    List<CartItem> items = cartService.getItems(cartId);
    return ResponseEntity.ok(new CartView(cart, items));
  }

  @PutMapping("/{cartId}/items")
  public ResponseEntity<CartPricingSnapshot> upsertItems(@PathVariable("cartId") String cartId,
      @Valid @RequestBody UpsertItemsRequest request) {
    CartPricingSnapshot snapshot = cartService.upsertItems(cartId, request.currency(),
        request.couponCode(), request.items().stream()
            .map(item -> new CartItemRequest(item.variantId(), item.categoryId(), item.quantity()))
            .toList());
    if (snapshot == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(snapshot);
  }

  @DeleteMapping("/{cartId}/items/{variantId}")
  public ResponseEntity<Void> removeItem(@PathVariable("cartId") String cartId,
      @PathVariable("variantId") String variantId) {
    if (!cartService.removeItem(cartId, variantId)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{cartId}")
  public ResponseEntity<Void> delete(@PathVariable("cartId") String cartId) {
    if (!cartService.deleteCart(cartId)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }

  public record CreateCartRequest(@NotBlank String userId) {
  }

  public record UpsertItemsRequest(
      @NotBlank String currency,
      String couponCode,
      @Valid List<CartItemInput> items
  ) {
  }

  public record CartItemInput(@NotBlank String variantId, String categoryId, @Min(1) int quantity) {
  }

  public record CartView(Cart cart, List<CartItem> items) {
  }
}
