#!/usr/bin/env bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Make sure we're running from the project root directory
cd $BASEDIR

case "$1" in
  all-dc-micrometer)
    nohup ./runme.sh dc-spring > spring.out &!
    nohup ./runme.sh dc-quarkus > quarkus.out &!
    nohup ./runme.sh dc-quarkus-native > quarkus-native.out &!
    exit
  ;;
  all-dc-mpmetrics)
    nohup ./runme.sh dc-mpmetrics > mppetrics.out &!
    nohup ./runme.sh dc-mpmetrics-native > mppetrics-native.out &!
    exit
  ;;
  dc-spring)
    URL=http://localhost:8280/combat/any
  ;;
  dc-quarkus)
    URL=http://localhost:8281/combat/any
  ;;
  dc-quarkus-native)
    URL=http://localhost:8283/combat/any
  ;;
  dc-mpmetrics)
    URL=http://localhost:8282/combat/any
  ;;
  dc-mpmetrics-native)
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
