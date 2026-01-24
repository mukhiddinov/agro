package com.agro.pricing.interfaces.rest;

import com.agro.pricing.application.PricingApplicationService;
import com.agro.pricing.application.PricingApplicationService.PricingItemRequest;
import com.agro.pricing.application.PricingApplicationService.PricingRequest;
import com.agro.pricing.application.PricingApplicationService.PricingResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pricing")
public class PricingController {
  private final PricingApplicationService pricingService;

  public PricingController(PricingApplicationService pricingService) {
    this.pricingService = pricingService;
  }

  @PostMapping("/quote")
  public ResponseEntity<PricingResult> quote(@Valid @RequestBody QuoteRequest request) {
    PricingRequest pricingRequest = new PricingRequest(request.currency(), request.couponCode(),
        request.items().stream()
            .map(item -> new PricingItemRequest(item.variantId(), item.quantity(), item.categoryId()))
            .toList());
    return ResponseEntity.ok(pricingService.quote(pricingRequest));
  }

  public record QuoteRequest(
      @NotBlank String currency,
      String couponCode,
      @Valid List<QuoteItem> items
  ) {
  }

  public record QuoteItem(
      @NotBlank String variantId,
      @Min(1) int quantity,
      String categoryId
  ) {
  }
}
