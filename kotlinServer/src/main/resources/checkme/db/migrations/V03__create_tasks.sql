DROP TABLE IF EXISTS tasks;

CREATE TYPE answer_format AS ENUM (
    'FILE',
    'TEXT'
);

CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    criterions JSONB NOT NULL,
    answerFormat answer_format NOT NULL,
    description TEXT NOT NULL
);