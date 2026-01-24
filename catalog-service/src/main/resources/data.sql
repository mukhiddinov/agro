INSERT INTO categories (id, name, parent_id, slug, sort_order, active, created_at, updated_at)
VALUES ('CAT-1', 'Apparel', NULL, 'apparel', 1, true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, sku, name, description, brand, primary_category_id, status, created_at, updated_at)
VALUES ('PROD-1', 'SKU-TSHIRT', 'Basic T-Shirt', 'Cotton t-shirt', 'AgroBasics', 'CAT-1', 'ACTIVE', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO variants (id, product_id, sku, attributes_json, status, created_at, updated_at)
VALUES ('VAR-1', 'PROD-1', 'SKU-TSHIRT-S', '{"size":"S","color":"white"}', 'ACTIVE', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO availability_snapshots (variant_id, available, last_sync_at)
VALUES ('VAR-1', true, now())
ON CONFLICT (variant_id) DO NOTHING;
