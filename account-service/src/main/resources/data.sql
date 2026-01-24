INSERT INTO accounts (id, owner_id, type, balance)
VALUES ('BA-1', 'B-1', 'BUYER', 1000),
       ('SA-1', 'S-1', 'SELLER', 0)
ON CONFLICT (id) DO NOTHING;
