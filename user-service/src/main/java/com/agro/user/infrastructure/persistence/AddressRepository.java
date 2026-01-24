package com.agro.user.infrastructure.persistence;

import com.agro.user.domain.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
  Optional<Address> findByIdAndUserId(String id, String userId);
  List<Address> findByUserId(String userId);
}
