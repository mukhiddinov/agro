package com.agro.catalog.infrastructure.persistence;

import com.agro.catalog.domain.Product;
import com.agro.catalog.domain.ProductStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
  List<Product> findByPrimaryCategoryId(String primaryCategoryId);
  List<Product> findByPrimaryCategoryIdAndStatus(String primaryCategoryId, ProductStatus status);
  List<Product> findByStatus(ProductStatus status);
}
