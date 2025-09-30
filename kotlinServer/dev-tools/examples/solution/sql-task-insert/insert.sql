INSERT INTO books (title, author_id, publication_year)
VALUES ('Талисман', (SELECT id FROM authors WHERE name = 'Стивен Кинг'), 1984);