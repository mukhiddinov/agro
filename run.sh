#!/usr/bin/env bash
set -euo pipefail

USER_URL="http://localhost:8088"
CATALOG_URL="http://localhost:8083"
PRICING_URL="http://localhost:8084"
CART_URL="http://localhost:8085"
CHECKOUT_URL="http://localhost:8086"
ORDER_URL="http://localhost:8080"
SHIPPING_URL="http://localhost:8089"

log() { echo -e "\n==== $* ===="; }
fail() { echo "❌ $1"; exit 1; }

http_check() {
  local method=$1 url=$2 body=${3:-}
  local code
  if [[ -n "$body" ]]; then
    code=$(curl -s -o /dev/null -w "%{http_code}" \
      -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -d "$body" || true)
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" \
      -X "$method" "$url" || true)
  fi

  [[ "$code" =~ ^2|^4 ]] || fail "$method $url failed (HTTP $code)"
}

log "USER SERVICE"
http_check GET  "$USER_URL/users/U-1"

log "CATALOG SERVICE"
http_check GET  "$CATALOG_URL/catalog/categories"
http_check GET  "$CATALOG_URL/catalog/products"

log "PRICING SERVICE"
http_check POST "$PRICING_URL/pricing/quote" \
  '{"currency":"USD","items":[{"variantId":"VAR-1","quantity":1,"categoryId":"CAT-1"}]}'

log "CART SERVICE"
http_check POST "$CART_URL/carts" '{"userId":"U-1"}'

log "SHIPPING SERVICE"
http_check POST "$SHIPPING_URL/shipping/options" \
  '{"address":{"city":"Tashkent","country":"UZ"},"items":[],"currency":"USD"}'

log "CHECKOUT SERVICE"
http_check POST "$CHECKOUT_URL/checkout" \
  '{"cartId":"x","userId":"U-1","currency":"USD","shippingOptionId":"STANDARD","paymentMethodId":"PM-1"}'

log "ORDER SERVICE"
http_check GET "$ORDER_URL/orders/health"

echo -e "\n✅ BACKEND IS FRONTEND-READY"
