package com.agro.catalog.infrastructure.persistence;

import com.agro.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
