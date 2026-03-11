SELECT title
FROM books
WHERE author_id = (SELECT id FROM authors WHERE name = 'Стивен Кинг') AND publication_year BETWEEN 1970 AND 1980;