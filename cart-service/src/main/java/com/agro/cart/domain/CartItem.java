package com.agro.cart.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {
  @Id
  private String id;

  private String cartId;
  private String variantId;
  private String categoryId;
  private int quantity;

  private String currency;
  private String pricingVersion;

  private BigDecimal baseUnitPrice;
  private BigDecimal discountUnitPrice;
  private BigDecimal finalUnitPrice;
  private BigDecimal lineTotal;
  private String taxClass;
  private boolean taxable;

  protected CartItem() {
  }

  public CartItem(String id, String cartId, String variantId, String categoryId, int quantity,
      String currency, String pricingVersion, BigDecimal baseUnitPrice, BigDecimal discountUnitPrice,
      BigDecimal finalUnitPrice, BigDecimal lineTotal, String taxClass, boolean taxable) {
    this.id = id;
    this.cartId = cartId;
    this.variantId = variantId;
    this.categoryId = categoryId;
    this.quantity = quantity;
    this.currency = currency;
    this.pricingVersion = pricingVersion;
    this.baseUnitPrice = baseUnitPrice;
    this.discountUnitPrice = discountUnitPrice;
    this.finalUnitPrice = finalUnitPrice;
    this.lineTotal = lineTotal;
    this.taxClass = taxClass;
    this.taxable = taxable;
  }

  public String getId() {
    return id;
  }

  public String getCartId() {
    return cartId;
  }

  public String getVariantId() {
    return variantId;
  }

  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getPricingVersion() {
    return pricingVersion;
  }

  public void setPricingVersion(String pricingVersion) {
    this.pricingVersion = pricingVersion;
  }

  public BigDecimal getBaseUnitPrice() {
    return baseUnitPrice;
  }

  public void setBaseUnitPrice(BigDecimal baseUnitPrice) {
    this.baseUnitPrice = baseUnitPrice;
  }

  public BigDecimal getDiscountUnitPrice() {
    return discountUnitPrice;
  }

  public void setDiscountUnitPrice(BigDecimal discountUnitPrice) {
    this.discountUnitPrice = discountUnitPrice;
  }

  public BigDecimal getFinalUnitPrice() {
    return finalUnitPrice;
  }

  public void setFinalUnitPrice(BigDecimal finalUnitPrice) {
    this.finalUnitPrice = finalUnitPrice;
  }

  public BigDecimal getLineTotal() {
    return lineTotal;
  }

  public void setLineTotal(BigDecimal lineTotal) {
    this.lineTotal = lineTotal;
  }

  public String getTaxClass() {
    return taxClass;
  }

  public void setTaxClass(String taxClass) {
    this.taxClass = taxClass;
  }

  public boolean isTaxable() {
    return taxable;
  }

  public void setTaxable(boolean taxable) {
    this.taxable = taxable;
  }
}
