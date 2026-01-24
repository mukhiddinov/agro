package com.agro.order.interfaces.rest;

import com.agro.order.application.OrderSagaService;
import com.agro.order.application.PlaceOrderCommand;
import com.agro.order.domain.Order;
import com.agro.order.infrastructure.persistence.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderSagaService sagaService;
  private final OrderRepository orderRepository;

  public OrderController(OrderSagaService sagaService, OrderRepository orderRepository) {
    this.sagaService = sagaService;
    this.orderRepository = orderRepository;
  }

  @Operation(summary = "Get order status")
  @ApiResponse(responseCode = "200", description = "Order found")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "500", description = "Server error")
  @GetMapping("/{orderId}")
  public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) {
    Order order = orderRepository.findById(orderId).orElse(null);
    if (order == null) {
      return ResponseEntity.status(404)
          .body(new ErrorResponse("RESOURCE_NOT_FOUND", "Order not found"));
    }
    String status = order.getStatus() == com.agro.order.domain.OrderStatus.PENDING
        ? "CREATED"
        : order.getStatus().name();
    return ResponseEntity.ok(new OrderStatusResponse(order.getId(), status,
        order.getCreatedAt(), order.getUpdatedAt()));
  }

  @Operation(summary = "Place order and start saga")
  @ApiResponse(responseCode = "202", description = "Order accepted")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "500", description = "Server error")
  @PostMapping
  public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
    String orderId = sagaService.placeOrder(new PlaceOrderCommand(
        request.productId(), request.quantity(), request.buyerId(), request.amount(), request.currency(),
        request.paymentMethodId(), request.shippingOptionId(), request.addressId()));
    return ResponseEntity.accepted().body(new OrderResponse(orderId));
  }

  public record PlaceOrderRequest(
      @NotBlank String productId,
      @Min(1) int quantity,
      @NotBlank String buyerId,
      @DecimalMin("0.01") double amount,
      @NotBlank String currency,
      @NotBlank String paymentMethodId,
      @NotBlank String shippingOptionId,
      @NotBlank String addressId
  ) {
  }

  public record OrderResponse(String orderId) {
  }

  public record OrderStatusResponse(
      @Schema(example = "ORDER-1") String id,
      @Schema(example = "READY_FOR_SHIPMENT") String status,
      Instant createdAt,
      Instant updatedAt
  ) {
  }

  public record ErrorResponse(
      @Schema(example = "RESOURCE_NOT_FOUND") String code,
      @Schema(example = "Order not found") String message
  ) {
  }
}
