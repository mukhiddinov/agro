package com.agro.checkout.interfaces.rest;

import com.agro.checkout.application.CheckoutApplicationService;
import com.agro.checkout.application.CheckoutApplicationService.CheckoutRequest;
import com.agro.checkout.application.CheckoutApplicationService.CheckoutResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {
  private final CheckoutApplicationService checkoutService;

  public CheckoutController(CheckoutApplicationService checkoutService) {
    this.checkoutService = checkoutService;
  }

  @PostMapping
  public ResponseEntity<CheckoutResult> checkout(@Valid @RequestBody CheckoutRequestBody request) {
    CheckoutResult result = checkoutService.checkout(new CheckoutRequest(
        request.cartId(), request.userId(), request.addressId(), request.currency(),
        request.couponCode(), request.shippingOptionId(), request.paymentMethodId()));
    return ResponseEntity.ok(result);
  }

  public record CheckoutRequestBody(
      @NotBlank String cartId,
      @NotBlank String userId,
      @NotBlank String addressId,
      @NotBlank String currency,
      String couponCode,
      @NotBlank String shippingOptionId,
      @NotBlank String paymentMethodId
  ) {
  }
}
