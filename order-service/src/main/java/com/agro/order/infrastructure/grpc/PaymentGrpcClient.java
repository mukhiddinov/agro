package com.agro.order.infrastructure.grpc;

import com.agro.contracts.AuthorizePaymentRequest;
import com.agro.contracts.CapturePaymentRequest;
import com.agro.contracts.CommandReply;
import com.agro.contracts.PaymentServiceGrpc;
import com.agro.contracts.RefundPaymentRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class PaymentGrpcClient {
  @GrpcClient("payment")
  private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

  public CommandReply authorize(String commandId, String orderId, String buyerId,
      String paymentMethodId, double amount, String currency) {
    return paymentStub.authorizePayment(AuthorizePaymentRequest.newBuilder()
        .setCommandId(commandId)
        .setOrderId(orderId)
        .setBuyerId(buyerId)
        .setPaymentMethodId(paymentMethodId)
        .setAmount(amount)
        .setCurrency(currency)
        .build());
  }

  public CommandReply capture(String commandId, String orderId, double amount, String currency) {
    return paymentStub.capturePayment(CapturePaymentRequest.newBuilder()
        .setCommandId(commandId)
        .setOrderId(orderId)
        .setAmount(amount)
        .setCurrency(currency)
        .build());
  }

  public CommandReply refund(String commandId, String orderId, double amount, String currency) {
    return paymentStub.refundPayment(RefundPaymentRequest.newBuilder()
        .setCommandId(commandId)
        .setOrderId(orderId)
        .setAmount(amount)
        .setCurrency(currency)
        .build());
  }
}
