#!/usr/bin/env bash

case "$1" in
  dc-spring)
    URL=http://localhost:8280/combat/any
  ;;
  dc-quarkus)
    URL=http://localhost:8281/combat/any
  ;;
  dc-mpmetrics)
    URL=http://localhost:8282/combat/any
  ;;
  k8s-spring)
    URL=http://monsters.192.168.99.100.nip.io/combat/faceoff
  ;;
  *)
  ;;
esac
if [ -z "$URL" ]; then
  URL=http://localhost:8080/combat/any
fi

while :
do
  curl $URL
  sleep 20
done
