#!/usr/bin/env bash

mkdir -p deploy/dc/target/prometheus
mkdir -p deploy/dc/target/grafana

root=$PWD

./mvnw install
