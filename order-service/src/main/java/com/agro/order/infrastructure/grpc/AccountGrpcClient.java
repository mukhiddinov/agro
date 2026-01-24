package com.agro.order.infrastructure.grpc;

import com.agro.contracts.AccountServiceGrpc;
import com.agro.contracts.CommandReply;
import com.agro.contracts.CreditSellerRequest;
import com.agro.contracts.DebitBuyerRequest;
import com.agro.contracts.RefundBuyerRequest;
import com.agro.contracts.ReverseSellerCreditRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class AccountGrpcClient {
  @GrpcClient("account")
  private AccountServiceGrpc.AccountServiceBlockingStub accountStub;

  public CommandReply debitBuyer(String commandId, String orderId, String buyerId, double amount) {
    return accountStub.debitBuyer(
        DebitBuyerRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setBuyerId(buyerId)
            .setAmount(amount)
            .build());
  }

  public CommandReply creditSeller(String commandId, String orderId, String sellerId, double amount) {
    return accountStub.creditSeller(
        CreditSellerRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setSellerId(sellerId)
            .setAmount(amount)
            .build());
  }

  public CommandReply refundBuyer(String commandId, String orderId, String buyerId, double amount) {
    return accountStub.refundBuyer(
        RefundBuyerRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setBuyerId(buyerId)
            .setAmount(amount)
            .build());
  }

  public CommandReply reverseSellerCredit(String commandId, String orderId, String sellerId, double amount) {
    return accountStub.reverseSellerCredit(
        ReverseSellerCreditRequest.newBuilder()
            .setCommandId(commandId)
            .setOrderId(orderId)
            .setSellerId(sellerId)
            .setAmount(amount)
            .build());
  }
}
