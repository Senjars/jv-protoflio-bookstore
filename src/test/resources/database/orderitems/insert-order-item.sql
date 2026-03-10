INSERT INTO users (id, email, password, first_name, last_name)
VALUES (997, 'testorderitem@test.com', 'password', 'Test', 'OrderItem');

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address)
VALUES (997, 997, 'PENDING', 99.99, '2024-01-01 12:00:00', 'Test Street 1');

INSERT INTO books (id, title, author, isbn, price)
VALUES (997, 'Hyperion', 'Dan Simmons', '978-0553283697', 45.00);

INSERT INTO order_items (id, order_id, book_id, quantity, price)
VALUES (997, 997, 997, 2, 45.00);