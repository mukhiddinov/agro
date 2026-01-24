package com.agro.inventory.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {
  public static final String TOPIC_RESERVED = "inventory.stock.reserved";
  public static final String TOPIC_RELEASED = "inventory.stock.released";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public InventoryEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(String topic, InventoryEvent event) {
    kafkaTemplate.send(topic, event.orderId(), toJson(event));
  }

  private String toJson(InventoryEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize inventory event", e);
    }
  }
}
