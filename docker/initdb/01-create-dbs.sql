CREATE USER "order" WITH PASSWORD 'order';
CREATE USER inventory WITH PASSWORD 'inventory';
CREATE USER account WITH PASSWORD 'account';
CREATE USER catalog WITH PASSWORD 'catalog';
CREATE USER pricing WITH PASSWORD 'pricing';
CREATE USER cart WITH PASSWORD 'cart';
CREATE USER payment WITH PASSWORD 'payment';
CREATE USER "user" WITH PASSWORD 'user';
CREATE USER shipping WITH PASSWORD 'shipping';

CREATE DATABASE orderdb OWNER "order";
CREATE DATABASE inventorydb OWNER inventory;
CREATE DATABASE accountdb OWNER account;
CREATE DATABASE catalogdb OWNER catalog;
CREATE DATABASE pricedb OWNER pricing;
CREATE DATABASE cartdb OWNER cart;
CREATE DATABASE paymentdb OWNER payment;
CREATE DATABASE userdb OWNER "user";
CREATE DATABASE shippingdb OWNER shipping;
