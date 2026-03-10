INSERT INTO categories (id, name) VALUES (1, 'Sci-Fi');

INSERT INTO books (id, title, author, isbn, price)
VALUES (1, 'Hyperion', 'Dan Simmons', '978-0553283686', 45.00);

INSERT INTO books_categories (book_id, category_id)
VALUES (1, 1);