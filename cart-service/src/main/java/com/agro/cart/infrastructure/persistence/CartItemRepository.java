package com.agro.cart.infrastructure.persistence;

import com.agro.cart.domain.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
  List<CartItem> findByCartId(String cartId);
  Optional<CartItem> findByCartIdAndVariantId(String cartId, String variantId);
  void deleteByCartIdAndVariantId(String cartId, String variantId);
}
