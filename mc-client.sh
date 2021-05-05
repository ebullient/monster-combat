#!/usr/bin/env bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Make sure we're running from the project root directory
cd $BASEDIR

case "$1" in
  all-micrometer)
    nohup ./mc-client.sh spring > out.client.spring &!
    nohup ./mc-client.sh quarkus > out.client.quarkus &!
    nohup ./mc-client.sh quarkus-native > out.client.quarkus.native &!
    exit
  ;;
  all-mpmetrics)
    nohup ./mc-client.sh mpmetrics > out.client.mpmetrics &!
    nohup ./mc-client.sh mpmetrics-native > out.client.mpmetrics.native &!
    exit
  ;;
  spring)
    URL=http://localhost:8280/combat/any
  ;;
  quarkus)
    URL=http://localhost:8281/combat/any
  ;;
  quarkus-native)
    URL=http://localhost:8283/combat/any
  ;;
  mpmetrics)
    URL=http://localhost:8282/combat/any
  ;;
  mpmetrics-native)
    URL=http://localhost:8284/combat/any
  ;;
  k8s-spring)
    URL=http://monsters.192.168.99.100.nip.io/combat/faceoff
  ;;
  *)
    URL=$1
  ;;
esac

if [ -z "$URL" ]; then
  URL=http://localhost:8080/combat/any
fi

while :
do
  curl $URL
  echo "---"
  sleep $[ ( $RANDOM % 10 ) + 1 ]s
done
