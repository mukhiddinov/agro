INSERT INTO users (id, email, name, created_at, updated_at)
VALUES ('U-1', 'buyer@example.com', 'Buyer One', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO addresses (id, user_id, line1, city, country, postal_code, created_at, updated_at)
VALUES ('ADDR-1', 'U-1', '123 Market St', 'City', 'US', '10001', now(), now())
ON CONFLICT (id) DO NOTHING;
