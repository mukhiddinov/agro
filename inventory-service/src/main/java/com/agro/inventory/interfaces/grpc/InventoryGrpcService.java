package com.agro.inventory.interfaces.grpc;

import com.agro.contracts.CommandReply;
import com.agro.contracts.InventoryServiceGrpc;
import com.agro.contracts.ReleaseStockRequest;
import com.agro.contracts.ReserveStockRequest;
import com.agro.inventory.application.InventoryApplicationService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {
  private final InventoryApplicationService inventoryService;

  public InventoryGrpcService(InventoryApplicationService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  public void reserveStock(ReserveStockRequest request, StreamObserver<CommandReply> responseObserver) {
    InventoryApplicationService.InventoryResult result = inventoryService.reserve(
        request.getCommandId(), request.getOrderId(), request.getProductId(), request.getQuantity());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void releaseStock(ReleaseStockRequest request, StreamObserver<CommandReply> responseObserver) {
    InventoryApplicationService.InventoryResult result = inventoryService.release(
        request.getCommandId(), request.getOrderId(), request.getProductId());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }
}
