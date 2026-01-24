package com.agro.user.application;

import com.agro.user.domain.Address;
import com.agro.user.domain.User;
import com.agro.user.infrastructure.persistence.AddressRepository;
import com.agro.user.infrastructure.persistence.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserApplicationService {
  private final UserRepository userRepository;
  private final AddressRepository addressRepository;

  public UserApplicationService(UserRepository userRepository, AddressRepository addressRepository) {
    this.userRepository = userRepository;
    this.addressRepository = addressRepository;
  }

  @Transactional
  public User createUser(String email, String name) {
    Instant now = Instant.now();
    User user = new User(UUID.randomUUID().toString(), email, name, now, now);
    return userRepository.save(user);
  }

  public User getUser(String userId) {
    return userRepository.findById(userId).orElse(null);
  }

  @Transactional
  public Address addAddress(String userId, String line1, String city, String country, String postalCode) {
    Instant now = Instant.now();
    Address address = new Address(UUID.randomUUID().toString(), userId, line1, city, country,
        postalCode, now, now);
    return addressRepository.save(address);
  }

  public Address getAddress(String userId, String addressId) {
    return addressRepository.findByIdAndUserId(addressId, userId).orElse(null);
  }

  public java.util.List<Address> listAddresses(String userId) {
    return addressRepository.findByUserId(userId);
  }
}
