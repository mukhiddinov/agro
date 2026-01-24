package com.agro.payment.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
  public static final String TOPIC_AUTHORIZED = "payment.authorized";
  public static final String TOPIC_CAPTURED = "payment.captured";
  public static final String TOPIC_REFUNDED = "payment.refunded";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public PaymentEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(String topic, PaymentEvent event) {
    kafkaTemplate.send(topic, event.orderId(), toJson(event));
  }

  private String toJson(PaymentEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize payment event", e);
    }
  }
}
