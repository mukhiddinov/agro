package com.agro.account.interfaces.grpc;

import com.agro.account.application.AccountApplicationService;
import com.agro.contracts.AccountServiceGrpc;
import com.agro.contracts.CommandReply;
import com.agro.contracts.CreditSellerRequest;
import com.agro.contracts.DebitBuyerRequest;
import com.agro.contracts.RefundBuyerRequest;
import com.agro.contracts.ReverseSellerCreditRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase {
  private final AccountApplicationService accountService;

  public AccountGrpcService(AccountApplicationService accountService) {
    this.accountService = accountService;
  }

  @Override
  public void debitBuyer(DebitBuyerRequest request, StreamObserver<CommandReply> responseObserver) {
    AccountApplicationService.AccountResult result = accountService.debitBuyer(
        request.getCommandId(), request.getOrderId(), request.getBuyerId(), request.getAmount());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void creditSeller(CreditSellerRequest request, StreamObserver<CommandReply> responseObserver) {
    AccountApplicationService.AccountResult result = accountService.creditSeller(
        request.getCommandId(), request.getOrderId(), request.getSellerId(), request.getAmount());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void refundBuyer(RefundBuyerRequest request, StreamObserver<CommandReply> responseObserver) {
    AccountApplicationService.AccountResult result = accountService.refundBuyer(
        request.getCommandId(), request.getOrderId(), request.getBuyerId());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void reverseSellerCredit(ReverseSellerCreditRequest request,
      StreamObserver<CommandReply> responseObserver) {
    AccountApplicationService.AccountResult result = accountService.reverseSellerCredit(
        request.getCommandId(), request.getOrderId(), request.getSellerId());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }
}
