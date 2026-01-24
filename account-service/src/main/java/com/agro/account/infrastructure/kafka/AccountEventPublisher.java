package com.agro.account.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountEventPublisher {
  public static final String TOPIC_BUYER_DEBITED = "account.buyer.debited";
  public static final String TOPIC_BUYER_REFUNDED = "account.buyer.refunded";
  public static final String TOPIC_SELLER_CREDITED = "account.seller.credited";
  public static final String TOPIC_SELLER_REVERSED = "account.seller.credit.reversed";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public AccountEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(String topic, AccountEvent event) {
    kafkaTemplate.send(topic, event.orderId(), toJson(event));
  }

  private String toJson(AccountEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize account event", e);
    }
  }
}
