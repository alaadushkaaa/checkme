DROP TABLE IF EXISTS bundles;

CREATE TABLE bundles (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    tasks JSONB NOT NULL,
    isActual BOOL NOT NULL
);