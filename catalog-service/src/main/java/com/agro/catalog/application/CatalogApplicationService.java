package com.agro.catalog.application;

import com.agro.catalog.domain.AvailabilitySnapshot;
import com.agro.catalog.domain.Category;
import com.agro.catalog.domain.Product;
import com.agro.catalog.domain.ProductStatus;
import com.agro.catalog.domain.Variant;
import com.agro.catalog.domain.VariantStatus;
import com.agro.catalog.infrastructure.kafka.CatalogEvent;
import com.agro.catalog.infrastructure.kafka.CatalogEventPublisher;
import com.agro.catalog.infrastructure.persistence.AvailabilitySnapshotRepository;
import com.agro.catalog.infrastructure.persistence.CategoryRepository;
import com.agro.catalog.infrastructure.persistence.ProductRepository;
import com.agro.catalog.infrastructure.persistence.VariantRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class CatalogApplicationService {
  private final ProductRepository productRepository;
  private final VariantRepository variantRepository;
  private final CategoryRepository categoryRepository;
  private final AvailabilitySnapshotRepository availabilityRepository;
  private final CatalogEventPublisher eventPublisher;

  public CatalogApplicationService(ProductRepository productRepository,
      VariantRepository variantRepository,
      CategoryRepository categoryRepository,
      AvailabilitySnapshotRepository availabilityRepository,
      CatalogEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
    this.categoryRepository = categoryRepository;
    this.availabilityRepository = availabilityRepository;
    this.eventPublisher = eventPublisher;
  }

  public Product getProduct(String productId) {
    return productRepository.findById(productId).orElse(null);
  }

  public Variant getVariant(String variantId) {
    return variantRepository.findById(variantId).orElse(null);
  }

  public List<Variant> getVariants(String productId) {
    return variantRepository.findByProductId(productId);
  }

  public List<Category> getCategories() {
    return categoryRepository.findAll();
  }

  public List<Product> listProducts(String categoryId, boolean activeOnly) {
    if (categoryId != null && !categoryId.isBlank()) {
      return activeOnly
          ? productRepository.findByPrimaryCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE)
          : productRepository.findByPrimaryCategoryId(categoryId);
    }
    return activeOnly
        ? productRepository.findByStatus(ProductStatus.ACTIVE)
        : productRepository.findAll();
  }

  public AvailabilitySnapshot getAvailability(String variantId) {
    return availabilityRepository.findById(variantId).orElse(null);
  }

  @Transactional
  public Product createProduct(String sku, String name, String description, String brand, String categoryId) {
    Instant now = Instant.now();
    Product product = new Product(UUID.randomUUID().toString(), sku, name, description, brand,
        categoryId, ProductStatus.ACTIVE, now, now);
    productRepository.save(product);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_PRODUCT_UPDATED,
        new CatalogEvent(product.getId(), "PRODUCT", "CREATED"));
    return product;
  }

  @Transactional
  public Product updateProduct(String productId, String name, String description, ProductStatus status) {
    Product product = productRepository.findById(productId).orElse(null);
    if (product == null) {
      return null;
    }
    product.setName(name);
    product.setDescription(description);
    product.setStatus(status);
    product.setUpdatedAt(Instant.now());
    productRepository.save(product);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_PRODUCT_UPDATED,
        new CatalogEvent(product.getId(), "PRODUCT", "UPDATED"));
    return product;
  }

  @Transactional
  public Category createCategory(String name, String parentId, String slug, int sortOrder) {
    Instant now = Instant.now();
    Category category = new Category(UUID.randomUUID().toString(), name, parentId, slug, sortOrder,
        true, now, now);
    categoryRepository.save(category);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_CATEGORY_UPDATED,
        new CatalogEvent(category.getId(), "CATEGORY", "CREATED"));
    return category;
  }

  @Transactional
  public Category updateCategory(String categoryId, String name, boolean active) {
    Category category = categoryRepository.findById(categoryId).orElse(null);
    if (category == null) {
      return null;
    }
    category.setName(name);
    category.setActive(active);
    category.setUpdatedAt(Instant.now());
    categoryRepository.save(category);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_CATEGORY_UPDATED,
        new CatalogEvent(category.getId(), "CATEGORY", "UPDATED"));
    return category;
  }

  @Transactional
  public Variant createVariant(String productId, String sku, String attributesJson) {
    Instant now = Instant.now();
    Variant variant = new Variant(UUID.randomUUID().toString(), productId, sku, attributesJson,
        VariantStatus.ACTIVE, now, now);
    variantRepository.save(variant);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_VARIANT_UPDATED,
        new CatalogEvent(variant.getId(), "VARIANT", "CREATED"));
    return variant;
  }

  @Transactional
  public Variant updateVariant(String variantId, String attributesJson, VariantStatus status) {
    Variant variant = variantRepository.findById(variantId).orElse(null);
    if (variant == null) {
      return null;
    }
    variant.setAttributesJson(attributesJson);
    variant.setStatus(status);
    variant.setUpdatedAt(Instant.now());
    variantRepository.save(variant);
    registerEventAfterCommit(CatalogEventPublisher.TOPIC_VARIANT_UPDATED,
        new CatalogEvent(variant.getId(), "VARIANT", "UPDATED"));
    return variant;
  }

  public record ValidationItem(String variantId, int quantity) {
  }

  public record ValidationResult(String variantId, boolean valid, String reason, boolean available) {
  }

  public List<ValidationResult> validate(List<ValidationItem> items) {
    return items.stream().map(item -> {
      Variant variant = variantRepository.findById(item.variantId()).orElse(null);
      if (variant == null || variant.getStatus() != VariantStatus.ACTIVE) {
        return new ValidationResult(item.variantId(), false, "Variant not found", false);
      }
      AvailabilitySnapshot availability = availabilityRepository.findById(item.variantId()).orElse(null);
      boolean available = availability != null && availability.isAvailable();
      if (!available) {
        return new ValidationResult(item.variantId(), false, "Not available", false);
      }
      return new ValidationResult(item.variantId(), true, "OK", true);
    }).toList();
  }

  private void registerEventAfterCommit(String topic, CatalogEvent event) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        eventPublisher.publish(topic, event);
      }
    });
  }
}
