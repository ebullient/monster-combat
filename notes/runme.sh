#!/bin/bash

URL=$1
if [ -z "$URL" ]; then
  URL=http://monsters.192.168.99.100.nip.io/battle/faceoff
fi

while :
do
  curl $URL
  sleep 3
done
