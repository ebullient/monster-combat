#!/usr/bin/env bash

mkdir -p deploy/dc/target/prometheus
mkdir -p deploy/dc/target/grafana
mkdir -p deploy/k8s/config

root=$PWD

./mvnw install
