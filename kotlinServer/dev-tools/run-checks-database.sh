#!/bin/bash

[[ -d mariadb-data ]] || mkdir mariadb-data

podman run -d --name checkme-checks \
  -e MARIADB_ROOT_PASSWORD=ppassword \
  -e MARIADB_DATABASE=checkmedb \
  -v ./mariadb-data:/var/lib/mysql \
  -p 3306:3306 \
  docker.io/library/mariadb:11.8

