ALTER TABLE bundle_tasks DROP CONSTRAINT IF EXISTS bundle_tasks_task_id_fkey;
ALTER TABLE bundle_tasks DROP CONSTRAINT IF EXISTS bundle_tasks_bundle_id_fkey;

ALTER TABLE users ADD COLUMN id_new UUID NOT NULL UNIQUE DEFAULT UUIDV7();
ALTER TABLE tasks ADD COLUMN id_new UUID NOT NULL UNIQUE DEFAULT UUIDV7();
ALTER TABLE bundles ADD COLUMN id_new UUID NOT NULL UNIQUE DEFAULT UUIDV7();
ALTER TABLE checks ADD COLUMN id_new UUID NOT NULL UNIQUE DEFAULT UUIDV7();
ALTER TABLE checks ADD COLUMN taskid_new UUID;
ALTER TABLE checks ADD COLUMN userid_new UUID;
ALTER TABLE bundle_tasks ADD COLUMN bundle_id_new UUID NOT NULL references bundles(id_new);
ALTER TABLE bundle_tasks ADD COLUMN task_id_new UUID NOT NULL references tasks(id_new);

ALTER TABLE users DROP id;
ALTER TABLE tasks DROP id;
ALTER TABLE bundles DROP id;
ALTER TABLE checks DROP id;
ALTER TABLE checks DROP taskid;
ALTER TABLE checks DROP userid;
ALTER TABLE bundle_tasks DROP bundle_id;
ALTER TABLE bundle_tasks DROP task_id;

ALTER TABLE users ADD PRIMARY KEY (id_new);
ALTER TABLE tasks ADD PRIMARY KEY (id_new);
ALTER TABLE bundles ADD PRIMARY KEY (id_new);
ALTER TABLE checks ADD PRIMARY KEY (id_new);
ALTER TABLE bundle_tasks ADD CONSTRAINT bundle_tasks_task_id_bundle_id_key UNIQUE (task_id_new, bundle_id_new);

ALTER TABLE users RENAME COLUMN id_new TO id;
ALTER TABLE tasks RENAME COLUMN id_new TO id;
ALTER TABLE bundles RENAME COLUMN id_new TO id;
ALTER TABLE checks RENAME COLUMN id_new TO id;
ALTER TABLE checks RENAME COLUMN taskid_new TO task_id;
ALTER TABLE checks RENAME COLUMN userid_new TO user_id;
ALTER TABLE bundle_tasks RENAME COLUMN task_id_new TO task_id;
ALTER TABLE bundle_tasks RENAME COLUMN bundle_id_new TO bundle_id;
