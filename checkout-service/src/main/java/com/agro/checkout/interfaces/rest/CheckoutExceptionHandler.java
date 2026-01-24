package com.agro.checkout.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CheckoutExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<String> handleServiceUnavailable(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
  }
}
