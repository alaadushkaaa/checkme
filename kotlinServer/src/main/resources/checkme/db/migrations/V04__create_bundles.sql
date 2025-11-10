DROP TABLE IF EXISTS bundles;

CREATE TABLE bundles (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    isActual BOOL NOT NULL
);

CREATE TABLE bundle_tasks (
    bundle_id INT NOT NULL REFERENCES bundles(id),
    task_id INT NOT NULL REFERENCES tasks(id),
    priority INT NOT NULL,
    UNIQUE (bundle_id, task_id)
);