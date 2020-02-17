#!/usr/bin/env bash

mkdir -p deploy/dc/target/prometheus
mkdir -p deploy/dc/target/grafana

root=$PWD

cd $root/core
../mvnw install

cd $root/spring5-micrometer
../mvnw install
# anything else for the docker image?

cd $root/quarkus-micrometer
../mvnw install
docker build -f src/main/docker/Dockerfile.jvm -t ebullient/dnd-mc-quarkus-micrometer .