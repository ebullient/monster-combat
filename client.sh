#!/usr/bin/env bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Make sure we're running from the project root directory
cd $BASEDIR

case "$1" in
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
  start)
    nohup ./client.sh spring > out.client.spring &
    nohup ./client.sh quarkus > out.client.quarkus &
    nohup ./client.sh quarkus-native > out.client.quarkus.native &
    nohup ./client.sh mpmetrics > out.client.mpmetrics &
    nohup ./client.sh mpmetrics-native > out.client.mpmetrics.native &
    exec ./client.sh list
  ;;
  list)
    ps -A -o pid,command | grep client.sh | grep -v list | grep -v grep
    exit
  ;;
  stop)
    pids=$(ps -A -o pid,command | awk '{$1=$1};1' | grep client.sh | grep -v stop | grep -v grep | cut -d ' ' -f1)
    for x in $pids; do echo "stopping $x"; kill $x; done
    exit
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
  curl --no-progress-meter $URL
  echo "---"
  sleep $[ ( $RANDOM % 10 ) + 1 ]s
done
