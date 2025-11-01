DROP TABLE IF EXISTS tasks;

CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    criterions JSONB NOT NULL,
    answerFormat JSONB NOT NULL,
    description TEXT NOT NULL,
    is_actual BOOL NOT NULL
);