# AgroPlatform API Documentation

## user-service
Purpose: User profiles and address management for checkout.

Base URL: `http://localhost:8088`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/users` | Create a user profile |
| GET | `/users/{userId}` | Fetch user profile |
| POST | `/users/{userId}/addresses` | Add address to user |
| GET | `/users/{userId}/addresses/{addressId}` | Fetch a user address |

### POST /users
Create a user profile for onboarding and checkout.

**Headers**
- `Content-Type: application/json`

**Request body (JSON schema)**
```json
{
  "type": "object",
  "required": ["email", "name"],
  "properties": {
    "email": { "type": "string" },
    "name": { "type": "string" }
  }
}
```

**Example request**
```json
{ "email": "buyer@example.com", "name": "Buyer One" }
```

**Success response (200)**
```json
{
  "id": "U-1",
  "email": "buyer@example.com",
  "name": "Buyer One",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 400: Validation error (missing/blank fields)
- 500: Internal server error

### GET /users/{userId}
Fetch profile details for account and checkout.

**Path params**
- `userId` (string)

**Success response (200)**
```json
{
  "id": "U-1",
  "email": "buyer@example.com",
  "name": "Buyer One",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 404: User not found
- 500: Internal server error

### POST /users/{userId}/addresses
Add a shipping address for checkout.

**Headers**
- `Content-Type: application/json`

**Path params**
- `userId` (string)

**Request body (JSON schema)**
```json
{
  "type": "object",
  "required": ["line1", "city", "country", "postalCode"],
  "properties": {
    "line1": { "type": "string" },
    "city": { "type": "string" },
    "country": { "type": "string" },
    "postalCode": { "type": "string" }
  }
}
```

**Example request**
```json
{
  "line1": "123 Market St",
  "city": "City",
  "country": "US",
  "postalCode": "10001"
}
```

**Success response (200)**
```json
{
  "id": "ADDR-1",
  "userId": "U-1",
  "line1": "123 Market St",
  "city": "City",
  "country": "US",
  "postalCode": "10001",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 400: Validation error
- 404: User not found (if internal lookup fails)
- 500: Internal server error

### GET /users/{userId}/addresses/{addressId}
Fetch a specific address for checkout prefill.

**Path params**
- `userId` (string)
- `addressId` (string)

**Success response (200)**
```json
{
  "id": "ADDR-1",
  "userId": "U-1",
  "line1": "123 Market St",
  "city": "City",
  "country": "US",
  "postalCode": "10001",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 404: Address not found
- 500: Internal server error

**Frontend usage notes**
- Account profile page: create/get user
- Checkout page: list/select address (you will need to store address IDs client-side; only GET by id exists)

---

## catalog-service
Purpose: Product catalog metadata, categories, variants, availability validation.

Base URL: `http://localhost:8083`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| GET | `/catalog/products/{productId}` | Product details |
| GET | `/catalog/products/{productId}/variants` | Variants for product |
| GET | `/catalog/variants/{variantId}` | Variant detail + availability |
| GET | `/catalog/categories` | Category list |
| POST | `/catalog/validate` | Validate purchasable variants |
| POST | `/catalog/admin/products` | Admin: create product |
| PATCH | `/catalog/admin/products/{productId}` | Admin: update product |
| POST | `/catalog/admin/categories` | Admin: create category |
| PATCH | `/catalog/admin/categories/{categoryId}` | Admin: update category |
| POST | `/catalog/admin/variants` | Admin: create variant |
| PATCH | `/catalog/admin/variants/{variantId}` | Admin: update variant |

### GET /catalog/products/{productId}
Product details for product page.

**Path params**
- `productId` (string)

**Success response (200)**
```json
{
  "id": "PROD-1",
  "sku": "SKU-1",
  "name": "T-Shirt",
  "description": "Soft cotton tee",
  "brand": "AgroBrand",
  "primaryCategoryId": "CAT-1",
  "status": "ACTIVE",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 404: Product not found
- 500: Internal server error

### GET /catalog/products/{productId}/variants
Variants for product display and selection.

**Path params**
- `productId` (string)

**Success response (200)**
```json
[
  {
    "variant": {
      "id": "VAR-1",
      "productId": "PROD-1",
      "sku": "SKU-1-S",
      "attributesJson": "{\"size\":\"S\",\"color\":\"blue\"}",
      "status": "ACTIVE",
      "createdAt": "2026-01-24T12:00:00Z",
      "updatedAt": "2026-01-24T12:00:00Z"
    },
    "available": true
  }
]
```

**Errors**
- 500: Internal server error

### GET /catalog/variants/{variantId}
Variant detail + availability.

**Path params**
- `variantId` (string)

**Success response (200)**
```json
{
  "variant": {
    "id": "VAR-1",
    "productId": "PROD-1",
    "sku": "SKU-1-S",
    "attributesJson": "{\"size\":\"S\",\"color\":\"blue\"}",
    "status": "ACTIVE",
    "createdAt": "2026-01-24T12:00:00Z",
    "updatedAt": "2026-01-24T12:00:00Z"
  },
  "available": true
}
```

**Errors**
- 404: Variant not found
- 500: Internal server error

### GET /catalog/categories
Category navigation and filters.

**Success response (200)**
```json
[
  {
    "id": "CAT-1",
    "name": "Apparel",
    "parentId": null,
    "slug": "apparel",
    "sortOrder": 1,
    "active": true,
    "createdAt": "2026-01-24T12:00:00Z",
    "updatedAt": "2026-01-24T12:00:00Z"
  }
]
```

**Errors**
- 500: Internal server error

### POST /catalog/validate
Validate items are active and available (used by cart/checkout).

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["items"],
  "properties": {
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["variantId", "quantity"],
        "properties": {
          "variantId": { "type": "string" },
          "quantity": { "type": "integer", "minimum": 1 }
        }
      }
    }
  }
}
```

**Example request**
```json
{
  "items": [
    { "variantId": "VAR-1", "quantity": 1 }
  ]
}
```

**Success response (200)**
```json
[
  { "variantId": "VAR-1", "valid": true, "message": "OK", "available": true }
]
```

**Errors**
- 400: Validation error (missing/blank fields)
- 500: Internal server error

### POST /catalog/admin/products
Admin create product (seller/backoffice).

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["sku", "name"],
  "properties": {
    "sku": { "type": "string" },
    "name": { "type": "string" },
    "description": { "type": "string" },
    "brand": { "type": "string" },
    "primaryCategoryId": { "type": "string" }
  }
}
```

**Example request**
```json
{
  "sku": "SKU-1",
  "name": "T-Shirt",
  "description": "Soft cotton tee",
  "brand": "AgroBrand",
  "primaryCategoryId": "CAT-1"
}
```

**Success response (200)**
```json
{
  "id": "PROD-1",
  "sku": "SKU-1",
  "name": "T-Shirt",
  "description": "Soft cotton tee",
  "brand": "AgroBrand",
  "primaryCategoryId": "CAT-1",
  "status": "ACTIVE",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 400: Validation error
- 500: Internal server error

### PATCH /catalog/admin/products/{productId}
Admin update product.

**Path params**
- `productId` (string)

**Request body**
```json
{
  "type": "object",
  "required": ["name"],
  "properties": {
    "name": { "type": "string" },
    "description": { "type": "string" },
    "status": { "type": "string", "enum": ["DRAFT", "ACTIVE", "ARCHIVED"] }
  }
}
```

**Success response (200)**: Product JSON (same as create)

**Errors**
- 404: Product not found
- 400: Validation error
- 500: Internal server error

### POST /catalog/admin/categories
Admin create category.

**Request body**
```json
{
  "type": "object",
  "required": ["name", "slug"],
  "properties": {
    "name": { "type": "string" },
    "parentId": { "type": "string" },
    "slug": { "type": "string" },
    "sortOrder": { "type": "integer" }
  }
}
```

**Success response (200)**: Category JSON

**Errors**
- 400: Validation error
- 500: Internal server error

### PATCH /catalog/admin/categories/{categoryId}
Admin update category.

**Path params**
- `categoryId` (string)

**Request body**
```json
{
  "type": "object",
  "required": ["name"],
  "properties": {
    "name": { "type": "string" },
    "active": { "type": "boolean" }
  }
}
```

**Success response (200)**: Category JSON

**Errors**
- 404: Category not found
- 400: Validation error
- 500: Internal server error

### POST /catalog/admin/variants
Admin create variant.

**Request body**
```json
{
  "type": "object",
  "required": ["productId", "sku"],
  "properties": {
    "productId": { "type": "string" },
    "sku": { "type": "string" },
    "attributesJson": { "type": "string" }
  }
}
```

**Success response (200)**: Variant JSON

**Errors**
- 400: Validation error
- 500: Internal server error

### PATCH /catalog/admin/variants/{variantId}
Admin update variant.

**Path params**
- `variantId` (string)

**Request body**
```json
{
  "type": "object",
  "properties": {
    "attributesJson": { "type": "string" },
    "status": { "type": "string", "enum": ["ACTIVE", "INACTIVE"] }
  }
}
```

**Success response (200)**: Variant JSON

**Errors**
- 404: Variant not found
- 400: Validation error
- 500: Internal server error

**Frontend usage notes**
- Product page: GET product + variants + pricing quote
- Category page: GET categories, filter products by category (needs additional listing endpoint; not present)
- Cart validation: `/catalog/validate` used by cart/checkout

---

## pricing-service
Purpose: Price quotes and discounts for cart/checkout.

Base URL: `http://localhost:8084`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/pricing/quote` | Price items in cart/checkout |

### POST /pricing/quote
Calculate pricing for items.

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["currency", "items"],
  "properties": {
    "currency": { "type": "string" },
    "couponCode": { "type": "string" },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["variantId", "quantity"],
        "properties": {
          "variantId": { "type": "string" },
          "quantity": { "type": "integer", "minimum": 1 },
          "categoryId": { "type": "string" }
        }
      }
    }
  }
}
```

**Example request**
```json
{
  "currency": "USD",
  "couponCode": null,
  "items": [
    { "variantId": "VAR-1", "quantity": 2, "categoryId": "CAT-1" }
  ]
}
```

**Success response (200)**
```json
{
  "pricingVersion": "1769260637353",
  "currency": "USD",
  "items": [
    {
      "variantId": "VAR-1",
      "quantity": 2,
      "baseUnitPrice": 50.00,
      "discountUnitPrice": 0.00,
      "finalUnitPrice": 50.00,
      "lineTotal": 100.00,
      "taxClass": "STANDARD",
      "taxable": true
    }
  ],
  "subtotal": 100.00,
  "discountTotal": 0.00,
  "taxableAmount": 100.00
}
```

**Errors**
- 400: Validation error
- 500: Internal server error

**Frontend usage notes**
- Cart page: update price snapshot after item changes
- Checkout page: re-quote with coupon and currency before placing order

---

## cart-service
Purpose: Cart lifecycle + item pricing snapshots.

Base URL: `http://localhost:8085`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/carts` | Create cart |
| GET | `/carts/{cartId}` | Fetch cart + items |
| PUT | `/carts/{cartId}/items` | Add/update items and pricing snapshot |
| DELETE | `/carts/{cartId}/items/{variantId}` | Remove item |
| DELETE | `/carts/{cartId}` | Delete cart |

### POST /carts
Create a new cart for a user.

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["userId"],
  "properties": {
    "userId": { "type": "string" }
  }
}
```

**Success response (200)**
```json
{
  "id": "CART-1",
  "userId": "U-1",
  "status": "ACTIVE",
  "createdAt": "2026-01-24T12:00:00Z",
  "updatedAt": "2026-01-24T12:00:00Z"
}
```

**Errors**
- 400: Validation error
- 500: Internal server error

### GET /carts/{cartId}
Fetch cart and items for cart page.

**Path params**
- `cartId` (string)

**Success response (200)**
```json
{
  "cart": {
    "id": "CART-1",
    "userId": "U-1",
    "status": "ACTIVE",
    "createdAt": "2026-01-24T12:00:00Z",
    "updatedAt": "2026-01-24T12:00:00Z"
  },
  "items": [
    {
      "id": "ITEM-1",
      "cartId": "CART-1",
      "variantId": "VAR-1",
      "categoryId": "CAT-1",
      "quantity": 1,
      "currency": "USD",
      "pricingVersion": "1769260637353",
      "baseUnitPrice": 50.00,
      "discountUnitPrice": 0.00,
      "finalUnitPrice": 50.00,
      "lineTotal": 50.00,
      "taxClass": "STANDARD",
      "taxable": true
    }
  ]
}
```

**Errors**
- 404: Cart not found
- 500: Internal server error

### PUT /carts/{cartId}/items
Upsert items and refresh pricing snapshot.

**Headers**
- `Content-Type: application/json`

**Path params**
- `cartId` (string)

**Request body**
```json
{
  "type": "object",
  "required": ["currency", "items"],
  "properties": {
    "currency": { "type": "string" },
    "couponCode": { "type": "string" },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["variantId", "quantity"],
        "properties": {
          "variantId": { "type": "string" },
          "categoryId": { "type": "string" },
          "quantity": { "type": "integer", "minimum": 1 }
        }
      }
    }
  }
}
```

**Success response (200)**
```json
{
  "currency": "USD",
  "pricingVersion": "1769260637353",
  "items": [
    {
      "variantId": "VAR-1",
      "quantity": 1,
      "baseUnitPrice": 50.00,
      "discountUnitPrice": 0.00,
      "finalUnitPrice": 50.00,
      "lineTotal": 50.00,
      "taxClass": "STANDARD",
      "taxable": true
    }
  ],
  "subtotal": 50.00,
  "discountTotal": 0.00,
  "taxableAmount": 50.00
}
```

**Errors**
- 400: Invalid cart items (not purchasable) or validation error
- 404: Cart not found
- 503: Upstream service unavailable (catalog/pricing)
- 500: Internal server error

### DELETE /carts/{cartId}/items/{variantId}
Remove item from cart.

**Path params**
- `cartId` (string)
- `variantId` (string)

**Success response (204)**
No body.

**Errors**
- 404: Cart or item not found
- 500: Internal server error

### DELETE /carts/{cartId}
Delete cart.

**Path params**
- `cartId` (string)

**Success response (204)**
No body.

**Errors**
- 404: Cart not found
- 500: Internal server error

**Frontend usage notes**
- Cart page: GET cart, PUT items on update
- Mini-cart: GET cart summary
- Pricing is refreshed on upsert; no separate pricing call needed on cart page

---

## checkout-service
Purpose: Validate cart + address + shipping and create an Order.

Base URL: `http://localhost:8086`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/checkout` | Validate cart and place order |

### POST /checkout
Validate cart, address, shipping option, and create an order (returns `orderId`).

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["cartId", "userId", "addressId", "currency", "shippingOptionId", "paymentMethodId"],
  "properties": {
    "cartId": { "type": "string" },
    "userId": { "type": "string" },
    "addressId": { "type": "string" },
    "currency": { "type": "string" },
    "couponCode": { "type": "string" },
    "shippingOptionId": { "type": "string" },
    "paymentMethodId": { "type": "string" }
  }
}
```

**Example request**
```json
{
  "cartId": "CART-1",
  "userId": "U-1",
  "addressId": "ADDR-1",
  "currency": "USD",
  "couponCode": null,
  "shippingOptionId": "STANDARD",
  "paymentMethodId": "PM-OK"
}
```

**Success response (200)**
```json
{ "orderId": "ORDER-1" }
```

**Errors**
- 400: Validation error, cart invalid, address invalid, shipping option invalid
- 503: Pricing/Catalog/User/Shipping/Order service unavailable
- 500: Internal server error

**Frontend usage notes**
- Checkout page: call once after user confirms address + shipping + payment method
- This is not a saga; it only creates the order and returns `orderId`

---

## order-service
Purpose: Create order and orchestrate saga (inventory + payment + shipping).

Base URL: `http://localhost:8080`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/orders` | Create order and start saga |

### POST /orders
Create order and start saga workflow.

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["productId", "quantity", "buyerId", "amount", "currency", "paymentMethodId", "shippingOptionId", "addressId"],
  "properties": {
    "productId": { "type": "string" },
    "quantity": { "type": "integer", "minimum": 1 },
    "buyerId": { "type": "string" },
    "amount": { "type": "number", "minimum": 0.01 },
    "currency": { "type": "string" },
    "paymentMethodId": { "type": "string" },
    "shippingOptionId": { "type": "string" },
    "addressId": { "type": "string" }
  }
}
```

**Example request**
```json
{
  "productId": "VAR-1",
  "quantity": 1,
  "buyerId": "U-1",
  "amount": 55.00,
  "currency": "USD",
  "paymentMethodId": "PM-OK",
  "shippingOptionId": "STANDARD",
  "addressId": "ADDR-1"
}
```

**Success response (202 Accepted)**
```json
{ "orderId": "ORDER-1" }
```

**Errors**
- 400: Validation error
- 500: Internal server error (gRPC/Kafka issues, unexpected saga errors)

**Frontend usage notes**
- Called by checkout-service; frontend should normally not call this directly
- There is no GET endpoint for order status yet (use backend events or DB in admin tooling)

---

## payment-service
Purpose: Payment operations are gRPC; only exposes HTTP health for UI tooling.

Base URL: `http://localhost:8087`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| GET | `/health` | Service health |

### GET /health
**Success response (200)**
```json
{ "status": "UP" }
```

**Errors**
- 500: Service not healthy

**Frontend usage notes**
- Only for health checks/debug; payment actions are not REST.

---

## shipping-service
Purpose: Shipping options and shipment creation.

Base URL: `http://localhost:8089`

**Endpoints**
| Method | Path | Description |
|---|---|---|
| POST | `/shipping/options` | Get shipping options |
| POST | `/shipping/shipments` | Create shipment |

### POST /shipping/options
Return available options for a cart/checkout.

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["address", "items", "currency"],
  "properties": {
    "address": {
      "type": "object",
      "required": ["line1", "city", "country", "postalCode"],
      "properties": {
        "line1": { "type": "string" },
        "city": { "type": "string" },
        "country": { "type": "string" },
        "postalCode": { "type": "string" }
      }
    },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["variantId"],
        "properties": {
          "variantId": { "type": "string" },
          "quantity": { "type": "integer" }
        }
      }
    },
    "currency": { "type": "string" }
  }
}
```

**Example request**
```json
{
  "address": { "line1": "123 Market St", "city": "City", "country": "US", "postalCode": "10001" },
  "items": [{ "variantId": "VAR-1", "quantity": 1 }],
  "currency": "USD"
}
```

**Success response (200)**
```json
[
  {
    "optionId": "STANDARD",
    "carrier": "AgroShip",
    "serviceLevel": "STANDARD",
    "amount": 5.00,
    "currency": "USD",
    "estimatedDelivery": "3-5 days"
  },
  {
    "optionId": "EXPRESS",
    "carrier": "AgroShip",
    "serviceLevel": "EXPRESS",
    "amount": 12.00,
    "currency": "USD",
    "estimatedDelivery": "1-2 days"
  }
]
```

**Errors**
- 400: Validation error
- 500: Internal server error

### POST /shipping/shipments
Create a shipment after order completion (used by order-service).

**Headers**
- `Content-Type: application/json`

**Request body**
```json
{
  "type": "object",
  "required": ["orderId", "addressId", "shippingOptionId"],
  "properties": {
    "orderId": { "type": "string" },
    "addressId": { "type": "string" },
    "shippingOptionId": { "type": "string" }
  }
}
```

**Success response (200)**
```json
{ "shipmentId": "SHIP-1" }
```

**Errors**
- 400: Validation error
- 500: Internal server error

**Frontend usage notes**
- Checkout page: `/shipping/options` to populate shipping choices
- Shipment creation is handled by order-service; frontend typically doesn’t call it

---

# Typical UI-to-API Mapping
- Product listing page: `GET /catalog/categories`, `GET /catalog/products/{productId}`, `GET /catalog/products/{productId}/variants`
- Product detail page: `GET /catalog/variants/{variantId}` + `POST /pricing/quote`
- Cart page: `POST /carts`, `GET /carts/{cartId}`, `PUT /carts/{cartId}/items`
- Checkout page: `GET /users/{userId}`, `GET /users/{userId}/addresses/{addressId}`, `POST /shipping/options`, `POST /checkout`
- Order confirmation page: use `orderId` returned from checkout; no order GET exists yet
