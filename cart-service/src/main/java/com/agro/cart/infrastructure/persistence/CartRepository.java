package com.agro.cart.infrastructure.persistence;

import com.agro.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {
}
