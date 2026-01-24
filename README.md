# AgroPlatform - Minimal E-commerce Backend

This repo contains a minimal, production-oriented microservice backend implementing an orchestrated Saga with gRPC commands and Kafka events.

## Services
- `user-service`: user profile and address management (REST).
- `catalog-service`: product catalog, categories, variants (REST).
- `pricing-service`: pricing quotes (REST).
- `cart-service`: cart lifecycle and pricing snapshots (REST).
- `checkout-service`: validates cart and creates orders (REST).
- `order-service`: saga orchestrator, order creation/status (REST), gRPC clients, Kafka saga events.
- `inventory-service`: stock reservation/release (gRPC server), Kafka inventory events.
- `payment-service`: authorize/capture/refund (gRPC server), REST health endpoint, Kafka payment events.
- `account-service`: buyer debit / seller credit (gRPC server), Kafka account events.
- `shipping-service`: shipping options + shipment creation (REST).
- `contracts`: shared protobuf definitions.

## Local dependencies
Use Docker Compose to run PostgreSQL and Kafka (Postgres exposed on `5433`):

```bash
docker compose up -d
```

## Build

```bash
mvn clean install
```

## Run
Start each service in its own terminal:

```bash
mvn -pl user-service spring-boot:run
mvn -pl catalog-service spring-boot:run
mvn -pl pricing-service spring-boot:run
mvn -pl cart-service spring-boot:run
mvn -pl checkout-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl account-service spring-boot:run
mvn -pl shipping-service spring-boot:run
```

## Swagger UI
Swagger UI is available on each service at:

```
http://localhost:{port}/swagger-ui.html
```

## Checkout to order (happy path)

```bash
curl -X POST http://localhost:8086/checkout \
  -H 'Content-Type: application/json' \
  -d '{"cartId":"CART-1","userId":"U-1","addressId":"ADDR-1","currency":"USD","shippingOptionId":"STANDARD","paymentMethodId":"PM-OK"}'
```

## Order status

```bash
curl http://localhost:8080/orders/{orderId}
```

## Notes
- Databases are auto-created in PostgreSQL by `docker/initdb/01-create-dbs.sql`.
- REST ports:
  - order `8080`, catalog `8083`, pricing `8084`, cart `8085`, checkout `8086`,
    payment `8087`, user `8088`, shipping `8089`.
- gRPC ports: inventory `9091`, payment `9093`, account `9094`.
- Kafka broker: `localhost:9092`.
- For a real deployment, add migrations, observability, and retries with backoff.
