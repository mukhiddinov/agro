package com.agro.order.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SagaEventPublisher {
  public static final String TOPIC_SAGA_STARTED = "order.saga.started";
  public static final String TOPIC_SAGA_STEP_COMPLETED = "order.saga.step.completed";
  public static final String TOPIC_SAGA_STEP_FAILED = "order.saga.step.failed";
  public static final String TOPIC_SAGA_COMPENSATING = "order.saga.compensation.started";
  public static final String TOPIC_SAGA_COMPLETED = "order.saga.completed";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public SagaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publish(String topic, SagaEvent event) {
    kafkaTemplate.send(topic, event.orderId(), toJson(event));
  }

  private String toJson(SagaEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize saga event", e);
    }
  }
}
