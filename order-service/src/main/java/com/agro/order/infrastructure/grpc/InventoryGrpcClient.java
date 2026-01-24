package com.agro.order.infrastructure.grpc;

import com.agro.contracts.CommandReply;
import com.agro.contracts.InventoryServiceGrpc;
import com.agro.contracts.ReleaseStockRequest;
import com.agro.contracts.ReserveStockRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class InventoryGrpcClient {
  @GrpcClient("inventory")
  private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

  public CommandReply reserve(String commandId, String orderId, String productId, int quantity) {
    return inventoryStub.reserveStock(
        ReserveStockRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setProductId(productId)
            .setQuantity(quantity)
            .build());
  }

  public CommandReply release(String commandId, String orderId, String productId, int quantity) {
    return inventoryStub.releaseStock(
        ReleaseStockRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setProductId(productId)
            .setQuantity(quantity)
            .build());
  }
}
