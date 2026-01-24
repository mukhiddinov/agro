INSERT INTO products (product_id, available_stock)
VALUES ('P-1', 100)
ON CONFLICT (product_id) DO NOTHING;
