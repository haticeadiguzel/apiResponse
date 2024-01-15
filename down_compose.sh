#!/bin/bash

docker-compose -f db/docker-compose.yml down
docker-compose -f redis/docker-compose.yml down
