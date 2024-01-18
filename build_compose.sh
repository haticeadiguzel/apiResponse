#!/bin/bash

docker-compose -f db/docker-compose.yml up --build
docker-compose -f redis/docker-compose.yml up --build
docker-compose -f docker-compose.yml up --build
