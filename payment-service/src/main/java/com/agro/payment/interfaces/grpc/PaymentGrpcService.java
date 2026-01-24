package com.agro.payment.interfaces.grpc;

import com.agro.contracts.AuthorizePaymentRequest;
import com.agro.contracts.CapturePaymentRequest;
import com.agro.contracts.CommandReply;
import com.agro.contracts.PaymentServiceGrpc;
import com.agro.contracts.RefundPaymentRequest;
import com.agro.payment.application.PaymentApplicationService;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {
  private final PaymentApplicationService paymentService;

  public PaymentGrpcService(PaymentApplicationService paymentService) {
    this.paymentService = paymentService;
  }

  @Override
  public void authorizePayment(AuthorizePaymentRequest request, StreamObserver<CommandReply> responseObserver) {
    PaymentApplicationService.PaymentResult result = paymentService.authorize(
        request.getCommandId(),
        request.getOrderId(),
        request.getBuyerId(),
        request.getPaymentMethodId(),
        BigDecimal.valueOf(request.getAmount()),
        request.getCurrency());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void capturePayment(CapturePaymentRequest request, StreamObserver<CommandReply> responseObserver) {
    PaymentApplicationService.PaymentResult result = paymentService.capture(
        request.getCommandId(),
        request.getOrderId(),
        BigDecimal.valueOf(request.getAmount()),
        request.getCurrency());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }

  @Override
  public void refundPayment(RefundPaymentRequest request, StreamObserver<CommandReply> responseObserver) {
    PaymentApplicationService.PaymentResult result = paymentService.refund(
        request.getCommandId(),
        request.getOrderId(),
        BigDecimal.valueOf(request.getAmount()),
        request.getCurrency());
    responseObserver.onNext(CommandReply.newBuilder()
        .setSuccess(result.success())
        .setMessage(result.message())
        .build());
    responseObserver.onCompleted();
  }
}
