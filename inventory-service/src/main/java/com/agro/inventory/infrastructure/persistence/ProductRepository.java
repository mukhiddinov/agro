package com.agro.inventory.infrastructure.persistence;

import com.agro.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
