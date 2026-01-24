package com.agro.catalog.interfaces.rest;

import com.agro.catalog.application.CatalogApplicationService;
import com.agro.catalog.application.CatalogApplicationService.ValidationItem;
import com.agro.catalog.application.CatalogApplicationService.ValidationResult;
import com.agro.catalog.domain.AvailabilitySnapshot;
import com.agro.catalog.domain.Category;
import com.agro.catalog.domain.Product;
import com.agro.catalog.domain.Variant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
  private final CatalogApplicationService catalogService;

  public CatalogController(CatalogApplicationService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping("/products/{productId}")
  public ResponseEntity<Product> getProduct(@PathVariable("productId") String productId) {
    Product product = catalogService.getProduct(productId);
    if (product == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(product);
  }

  @Operation(summary = "List products")
  @ApiResponse(responseCode = "200", description = "Product list")
  @ApiResponse(responseCode = "500", description = "Server error")
  @GetMapping("/products")
  public ResponseEntity<?> listProducts(
      @RequestParam(value = "categoryId", required = false) String categoryId,
      @RequestParam(value = "active", defaultValue = "true") boolean active) {
    try {
      List<ProductSummary> products = catalogService.listProducts(categoryId, active).stream()
          .map(product -> new ProductSummary(product.getId(), product.getName(),
              product.getBrand(), product.getPrimaryCategoryId(),
              product.getStatus().name(), product.getCreatedAt(), product.getUpdatedAt()))
          .toList();
      return ResponseEntity.ok(products);
    } catch (RuntimeException ex) {
      return ResponseEntity.status(500)
          .body(new ErrorResponse("INTERNAL_ERROR", "Unable to list products"));
    }
  }

  @GetMapping("/products/{productId}/variants")
  public ResponseEntity<List<VariantView>> getVariants(@PathVariable("productId") String productId) {
    List<VariantView> views = catalogService.getVariants(productId).stream()
        .map(variant -> {
          AvailabilitySnapshot availability = catalogService.getAvailability(variant.getId());
          boolean available = availability != null && availability.isAvailable();
          return new VariantView(variant, available);
        })
        .toList();
    return ResponseEntity.ok(views);
  }

  @GetMapping("/variants/{variantId}")
  public ResponseEntity<VariantView> getVariant(@PathVariable("variantId") String variantId) {
    Variant variant = catalogService.getVariant(variantId);
    if (variant == null) {
      return ResponseEntity.notFound().build();
    }
    AvailabilitySnapshot availability = catalogService.getAvailability(variantId);
    boolean available = availability != null && availability.isAvailable();
    return ResponseEntity.ok(new VariantView(variant, available));
  }

  @GetMapping("/categories")
  public ResponseEntity<List<Category>> getCategories() {
    return ResponseEntity.ok(catalogService.getCategories());
  }

  @PostMapping("/validate")
  public ResponseEntity<List<ValidationResult>> validate(@Valid @RequestBody ValidationRequest request) {
    List<ValidationItem> items = request.items().stream()
        .map(item -> new ValidationItem(item.variantId(), item.quantity()))
        .toList();
    return ResponseEntity.ok(catalogService.validate(items));
  }

  public record ValidationRequest(@Valid List<ValidationItemRequest> items) {
  }

  public record ValidationItemRequest(@NotBlank String variantId, @Min(1) int quantity) {
  }

  public record ProductSummary(
      @Schema(example = "PROD-1") String id,
      @Schema(example = "T-Shirt") String name,
      @Schema(example = "AgroBrand") String brand,
      @Schema(example = "CAT-1") String primaryCategoryId,
      @Schema(example = "ACTIVE") String status,
      Instant createdAt,
      Instant updatedAt
  ) {
  }

  public record ErrorResponse(
      @Schema(example = "INTERNAL_ERROR") String code,
      @Schema(example = "Unable to list products") String message
  ) {
  }

  public record VariantView(Variant variant, boolean available) {
  }
}
