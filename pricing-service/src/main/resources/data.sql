INSERT INTO base_prices (id, variant_id, currency, amount, updated_at)
VALUES ('BP-1', 'VAR-1', 'USD', 25.00, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO tax_profiles (variant_id, tax_class, taxable, updated_at)
VALUES ('VAR-1', 'STANDARD', true, now())
ON CONFLICT (variant_id) DO NOTHING;

INSERT INTO promotion_rules (id, name, type, value, applies_to, target_id, min_qty, active, starts_at, ends_at)
VALUES ('PR-1', 'Launch 10%', 'PERCENT', 10, 'VARIANT', 'VAR-1', 1, true, now(), null)
ON CONFLICT (id) DO NOTHING;

INSERT INTO coupon_rules (code, type, value, active, starts_at, ends_at)
VALUES ('WELCOME5', 'FLAT', 5.00, true, now(), null)
ON CONFLICT (code) DO NOTHING;
