INSERT INTO users (id, email, password, first_name, last_name)
VALUES (998, 'testorder@test.com', 'password', 'Test', 'Order');

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address)
VALUES (998, 998, 'PENDING', 99.99, '2024-01-01 12:00:00', 'Test Street 1');