#!/usr/bin/env bash

mkdir -p deploy/dc/target/data/prometheus
mkdir -p deploy/dc/target/data/grafana
mkdir -p deploy/k8s/config

root=$PWD

./mvnw install
