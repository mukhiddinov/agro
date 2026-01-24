#!/usr/bin/env bash
set -euo pipefail

### CONFIG
CATALOG_URL="http://localhost:8083"
CART_URL="http://localhost:8085"
CHECKOUT_URL="http://localhost:8086"
ORDER_URL="http://localhost:8080"
PAYMENT_URL="http://localhost:8087"
SHIPPING_URL="http://localhost:8089"

POSTGRES_CONTAINER=$(docker compose ps -q postgres)

log() { echo -e "\n==== $* ===="; }
die() { echo "❌ ERROR: $*" >&2; exit 1; }

http() {
  local method=$1 url=$2 body=${3:-}
  if [[ -n "$body" ]]; then
    curl -s -X "$method" "$url" -H "Content-Type: application/json" -d "$body"
  else
    curl -s -X "$method" "$url"
  fi
}

json() {
  echo "$1" | sed -n "s/.*\"$2\":\"\([^\"]*\)\".*/\1/p"
}

psql() {
  docker exec -i "$POSTGRES_CONTAINER" \
    psql -U postgres -d "$1" -t -c "$2" | tr -d '[:space:]'
}

wait_for_status() {
  local order_id=$1 expected=$2
  for _ in {1..60}; do
    status=$(psql orderdb "SELECT status FROM orders WHERE id='$order_id';")
    [[ "$status" == "$expected" ]] && return 0
    sleep 2
  done
  die "Order $order_id not $expected"
}

# -----------------------------
log "SELLER: create product"
PRODUCT=$(http POST "$CATALOG_URL/catalog/admin/products" \
  '{"sku":"SKU-1","name":"Demo","description":"E2E","brand":"Test","primaryCategoryId":"CAT-1"}')
PRODUCT_ID=$(json "$PRODUCT" id)
[[ -n "$PRODUCT_ID" ]] || die "Product create failed"

log "SELLER: create variant"
VARIANT=$(http POST "$CATALOG_URL/catalog/admin/variants" \
  "{\"productId\":\"$PRODUCT_ID\",\"sku\":\"SKU-1-S\",\"attributesJson\":\"{\\\"size\\\":\\\"S\\\"}\"}")
VARIANT_ID=$(json "$VARIANT" id)
[[ -n "$VARIANT_ID" ]] || die "Variant create failed"

log "SELLER: seed price + inventory"
psql pricedb "INSERT INTO base_prices (id, variant_id, currency, amount, updated_at) VALUES ('BP-$VARIANT_ID', '$VARIANT_ID', 'USD', 50, now()) ON CONFLICT (variant_id, currency) DO UPDATE SET amount = EXCLUDED.amount, updated_at = now();"
psql inventorydb "INSERT INTO products (product_id,available_stock) VALUES ('$VARIANT_ID',100) ON CONFLICT DO NOTHING;"
psql catalogdb "INSERT INTO availability_snapshots (variant_id, available, last_sync_at) VALUES ('$VARIANT_ID', true, now()) ON CONFLICT (variant_id) DO UPDATE SET available = true, last_sync_at = now();"

# -----------------------------
log "BUYER: create cart"
CART=$(http POST "$CART_URL/carts" '{"userId":"U-1"}')
CART_ID=$(json "$CART" id)

log "BUYER: add item"
http PUT "$CART_URL/carts/$CART_ID/items" \
  "{\"currency\":\"USD\",\"items\":[{\"variantId\":\"$VARIANT_ID\",\"quantity\":1,\"categoryId\":\"CAT-1\"}]}"

log "BUYER: checkout (HAPPY PATH)"
CHECKOUT=$(http POST "$CHECKOUT_URL/checkout" \
  "{\"cartId\":\"$CART_ID\",\"userId\":\"U-1\",\"addressId\":\"ADDR-1\",\"paymentMethodId\":\"PM-OK\",\"shippingOptionId\":\"STANDARD\",\"currency\":\"USD\"}")
ORDER_ID=$(json "$CHECKOUT" orderId)
[[ -n "$ORDER_ID" ]] || die "Checkout failed"

wait_for_status "$ORDER_ID" "READY_FOR_SHIPMENT"

log "VERIFY: payment + inventory + shipment"
[[ "$(psql paymentdb "SELECT status FROM payments WHERE order_id='$ORDER_ID';")" == "CAPTURED" ]] || die "Payment not captured"
[[ "$(psql inventorydb "SELECT available_stock FROM products WHERE product_id='$VARIANT_ID';")" == "99" ]] || die "Stock not reduced"
[[ -n "$(psql shippingdb "SELECT id FROM shipments WHERE order_id='$ORDER_ID';")" ]] || die "Shipment missing"

# -----------------------------
log "SAGA FAIL 1: Payment fails → inventory restored"
psql inventorydb "UPDATE products SET available_stock=100 WHERE product_id='$VARIANT_ID';"

CART_FAIL=$(http POST "$CART_URL/carts" '{"userId":"U-1"}')
CID=$(json "$CART_FAIL" id)

http PUT "$CART_URL/carts/$CID/items" \
  "{\"currency\":\"USD\",\"items\":[{\"variantId\":\"$VARIANT_ID\",\"quantity\":1,\"categoryId\":\"CAT-1\"}]}"

CHECKOUT_FAIL=$(http POST "$CHECKOUT_URL/checkout" \
  "{\"cartId\":\"$CID\",\"userId\":\"U-1\",\"addressId\":\"ADDR-1\",\"paymentMethodId\":\"PM-FAIL\",\"shippingOptionId\":\"STANDARD\",\"currency\":\"USD\"}")
OID_FAIL=$(json "$CHECKOUT_FAIL" orderId)

wait_for_status "$OID_FAIL" "FAILED"

[[ "$(psql inventorydb "SELECT available_stock FROM products WHERE product_id='$VARIANT_ID';")" == "100" ]] \
  || die "Saga compensation failed"

log "✅ ALL E-COMMERCE SAGA TESTS PASSED"
