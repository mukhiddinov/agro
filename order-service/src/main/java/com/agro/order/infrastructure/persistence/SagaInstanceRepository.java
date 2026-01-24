package com.agro.order.infrastructure.persistence;

import com.agro.order.domain.SagaInstance;
import com.agro.order.domain.SagaState;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, String> {
  List<SagaInstance> findByStateNotIn(List<SagaState> states);
  List<SagaInstance> findByStateNotInAndLastUpdatedAtBefore(List<SagaState> states, Instant threshold);
}
