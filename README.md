# AgroPlatform - Minimal E-commerce Backend

This repo contains a minimal, production-oriented microservice backend implementing an orchestrated Saga with gRPC commands and Kafka events.

## Services
- `order-service`: saga orchestrator, REST API for placing orders, gRPC clients, Kafka saga events.
- `inventory-service`: stock reservation/release via gRPC, Kafka inventory events.
- `account-service`: buyer debit / seller credit via gRPC, Kafka account events.
- `contracts`: shared protobuf definitions.

## Local dependencies
Use Docker Compose to run PostgreSQL and Kafka:

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
mvn -pl order-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl account-service spring-boot:run
```

## Place an order

```bash
curl -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"P-1","quantity":2,"buyerId":"B-1","sellerId":"S-1","amount":50.0}'
```

## Notes
- Databases are auto-created in PostgreSQL by `docker/initdb/01-create-dbs.sql`.
- gRPC ports: inventory `9091`, account `9092`.
- Kafka broker: `localhost:9092`.
- For a real deployment, add migrations, observability, and retries with backoff.
