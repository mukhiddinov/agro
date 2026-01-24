package com.agro.shipping.interfaces.rest;

import com.agro.shipping.application.ShippingApplicationService;
import com.agro.shipping.application.ShippingApplicationService.ShippingOption;
import com.agro.shipping.domain.Shipment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipping")
public class ShippingController {
  private final ShippingApplicationService shippingService;

  public ShippingController(ShippingApplicationService shippingService) {
    this.shippingService = shippingService;
  }

  @PostMapping("/options")
  public ResponseEntity<List<ShippingOption>> options(@Valid @RequestBody ShippingOptionsRequest request) {
    return ResponseEntity.ok(shippingService.listOptions(request.currency()));
  }

  @PostMapping("/shipments")
  public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
    Shipment shipment = shippingService.createShipment(request.orderId(), request.addressId(),
        request.shippingOptionId());
    return ResponseEntity.ok(new ShipmentResponse(shipment.getId()));
  }

  public record ShippingAddress(@NotBlank String line1, @NotBlank String city,
      @NotBlank String country, @NotBlank String postalCode) {
  }

  public record ShippingItem(@NotBlank String variantId, int quantity) {
  }

  public record ShippingOptionsRequest(@Valid ShippingAddress address,
      @Valid List<ShippingItem> items, @NotBlank String currency) {
  }

  public record CreateShipmentRequest(
      @NotBlank String orderId,
      @NotBlank String addressId,
      @NotBlank String shippingOptionId
  ) {
  }

  public record ShipmentResponse(String shipmentId) {
  }
}
