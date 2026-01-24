package com.agro.catalog.interfaces.rest;

import com.agro.catalog.application.CatalogApplicationService;
import com.agro.catalog.domain.Category;
import com.agro.catalog.domain.Product;
import com.agro.catalog.domain.ProductStatus;
import com.agro.catalog.domain.Variant;
import com.agro.catalog.domain.VariantStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog/admin")
public class AdminCatalogController {
  private final CatalogApplicationService catalogService;

  public AdminCatalogController(CatalogApplicationService catalogService) {
    this.catalogService = catalogService;
  }

  @PostMapping("/products")
  public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
    Product product = catalogService.createProduct(request.sku(), request.name(), request.description(),
        request.brand(), request.primaryCategoryId());
    return ResponseEntity.ok(product);
  }

  @PatchMapping("/products/{productId}")
  public ResponseEntity<Product> updateProduct(@PathVariable("productId") String productId,
      @RequestBody UpdateProductRequest request) {
    Product product = catalogService.updateProduct(productId, request.name(), request.description(),
        request.status());
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(product);
  }

  @PostMapping("/categories")
  public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequest request) {
    Category category = catalogService.createCategory(request.name(), request.parentId(), request.slug(),
        request.sortOrder());
    return ResponseEntity.ok(category);
  }

  @PatchMapping("/categories/{categoryId}")
  public ResponseEntity<Category> updateCategory(@PathVariable("categoryId") String categoryId,
      @RequestBody UpdateCategoryRequest request) {
    Category category = catalogService.updateCategory(categoryId, request.name(), request.active());
    if (category == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(category);
  }

  @PostMapping("/variants")
  public ResponseEntity<Variant> createVariant(@RequestBody CreateVariantRequest request) {
    Variant variant = catalogService.createVariant(request.productId(), request.sku(),
        request.attributesJson());
    return ResponseEntity.ok(variant);
  }

  @PatchMapping("/variants/{variantId}")
  public ResponseEntity<Variant> updateVariant(@PathVariable("variantId") String variantId,
      @RequestBody UpdateVariantRequest request) {
    Variant variant = catalogService.updateVariant(variantId, request.attributesJson(), request.status());
    if (variant == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(variant);
  }

  public record CreateProductRequest(
      @NotBlank String sku,
      @NotBlank String name,
      String description,
      String brand,
      String primaryCategoryId
  ) {
  }

  public record UpdateProductRequest(
      @NotBlank String name,
      String description,
      ProductStatus status
  ) {
  }

  public record CreateCategoryRequest(
      @NotBlank String name,
      String parentId,
      @NotBlank String slug,
      int sortOrder
  ) {
  }

  public record UpdateCategoryRequest(
      @NotBlank String name,
      boolean active
  ) {
  }

  public record CreateVariantRequest(
      @NotBlank String productId,
      @NotBlank String sku,
      String attributesJson
  ) {
  }

  public record UpdateVariantRequest(
      String attributesJson,
      VariantStatus status
  ) {
  }
}
