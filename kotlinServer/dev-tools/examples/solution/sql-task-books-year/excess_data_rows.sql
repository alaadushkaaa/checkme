SELECT title
FROM books
WHERE author_id = (SELECT id FROM authors WHERE name = 'Стивен Кинг');