DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id = 999);

DELETE FROM orders WHERE user_id = 999;

DELETE FROM cart_items WHERE shopping_cart_id = 999;

DELETE FROM shopping_carts WHERE id = 999;

DELETE FROM users_roles WHERE user_id = 999;

DELETE FROM users WHERE id = 999;