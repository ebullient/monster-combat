#!/bin/bash

URL=$1
if [ -z "$URL" ]; then
  #URL=http://monsters.192.168.99.100.nip.io/combat/faceoff
  URL=http://localhost:8080/combat/any
fi

while :
do
  curl $URL
  sleep 20
done
