#!/bin/bash

docker-compose -f db/docker-compose.yml up -d
docker-compose -f redis/docker-compose.yml up -d
