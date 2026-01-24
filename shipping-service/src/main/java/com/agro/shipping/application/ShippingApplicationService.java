package com.agro.shipping.application;

import com.agro.shipping.domain.Shipment;
import com.agro.shipping.domain.ShipmentStatus;
import com.agro.shipping.infrastructure.persistence.ShipmentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingApplicationService {
  public record ShippingOption(
      String optionId,
      String carrier,
      String serviceLevel,
      BigDecimal amount,
      String currency,
      String estimatedDelivery
  ) {
  }

  private final ShipmentRepository shipmentRepository;

  public ShippingApplicationService(ShipmentRepository shipmentRepository) {
    this.shipmentRepository = shipmentRepository;
  }

  public List<ShippingOption> listOptions(String currency) {
    return List.of(
        new ShippingOption("STANDARD", "AgroShip", "STANDARD", new BigDecimal("5.00"),
            currency, "3-5 days"),
        new ShippingOption("EXPRESS", "AgroShip", "EXPRESS", new BigDecimal("12.00"),
            currency, "1-2 days"));
  }

  @Transactional
  public Shipment createShipment(String orderId, String addressId, String shippingOptionId) {
    Shipment existing = shipmentRepository.findByOrderId(orderId).orElse(null);
    if (existing != null) {
      return existing;
    }
    Shipment shipment = new Shipment(UUID.randomUUID().toString(), orderId, addressId,
        shippingOptionId, ShipmentStatus.CREATED, Instant.now());
    return shipmentRepository.save(shipment);
  }
}
