#!/bin/bash

[[ -d postgres-data ]] || mkdir postgres-data

podman run -d --name checkme-db \
  -e POSTGRES_PASSWORD=ppassword \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=checkmedb \
  -e PGDATA=/var/lib/postgresql/data/pgdata \
  -v ./postgres-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  docker.io/library/postgres:17.5

