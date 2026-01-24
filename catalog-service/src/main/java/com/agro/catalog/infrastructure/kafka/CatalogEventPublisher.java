package com.agro.catalog.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventPublisher {
  public static final String TOPIC_PRODUCT_UPDATED = "catalog.product.updated";
  public static final String TOPIC_CATEGORY_UPDATED = "catalog.category.updated";
  public static final String TOPIC_VARIANT_UPDATED = "catalog.variant.updated";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public CatalogEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(String topic, CatalogEvent event) {
    kafkaTemplate.send(topic, event.entityId(), toJson(event));
  }

  private String toJson(CatalogEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize catalog event", e);
    }
  }
}
