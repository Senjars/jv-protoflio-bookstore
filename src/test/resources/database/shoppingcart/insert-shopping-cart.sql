INSERT INTO users (id, email, password, first_name, last_name)
VALUES (999, 'testcart@test.com', 'password', 'Test', 'Cart');

INSERT INTO shopping_carts (id, user_id)
VALUES (999, 999);
