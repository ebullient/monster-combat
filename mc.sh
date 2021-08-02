#!/bin/bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

wrap_exec() {
  echo "
> $@"
  exec $@
}

wrap_launch() {
  out=$1
  shift
  echo "
> $@ > $out 2>&1 &"
  $@ > $out 2>&1 &
}

wrap_mvnw() {
  echo "
> $@"
  ./mvnw $@
}

usage() {
      echo "
mc.sh [--format|--native] [images|dc|jars|help]

  --format
    Flag that triggers formatting of source code.

  --native
    Flag that triggers use/inclusion of native image

Containers:

  images
    Action that invokes './mvnw clean package' with
    properties set to build container images and skip tests.
    If the native flag has been specified, additional properties
    are set to use a container to build native images.

  dc
    Invoke docker-compose using files in the deploy/dc directory.
    Additional command line arguments are passed to docker-compose.
    If the native flag has been specified, native images will be included
    in docker-compose operations.

Using jars directly:

  jars
    Build and package appropriate jars for all runtimes. If the
    native flag has been specified, also build native images.

  start
    Run jars directly (java -jar ... ). If the native flag has been
    specified, also start native binaries. Ports are adjusted to
    match localhost expectations in mc-client.sh

  list
    List running monster-combat processes (started with start)

  mem
    Show memory used by running monster-combat processes (once, using ps)

  memwatch
    Monitor memory used by running monster-combat processes (using top)

  stop
    Stop monster-combat processes (kill)

Example invocations:

  ./mc.sh
      Invokes ./mvnw clean install

  ./mc.sh --format
      Invokes ./mvnw clean install process-sources

  ./mc.sh --native
      Invokes ./mvnw clean install -Dnative

  ./mc.sh images
      Invokes ./mvnw clean package -DskipTests

  ./mc.sh --native images
      Creates native Quarkus container images using a container for build.
      These options are necessary to build native container images on
      Windows and MacOS.

      Invokes ./mvnw clean package \\
          -Dquarkus.container-image.build=true -DskipTests -Dnative \\
          -pl quarkus-micrometer,quarkus-mpmetrics

  ./mc.sh dc up -d
      If no override file exists:
        docker-compose -f ./deploy/dc/docker-compose.yml up -d
      otherwise:
        docker-compose -f ./deploy/dc/docker-compose.yml \\
                      -f ./deploy/dc/docker-compose.override.yml \\
                      up -d

  ./mc.sh --native dc up -d
        If no override file exists:
          docker-compose -f ./deploy/dc/docker-compose.yml \\
                        -f ./deploy/dc/docker-compose-native.yml \\
                        up -d
        otherwise:
          docker-compose -f ./deploy/dc/docker-compose.yml \\
                        -f ./deploy/dc/docker-compose-native.yml \\
                        -f ./deploy/dc/docker-compose.override.yml \\
                        up -d
"
}


# Ensure we're executing from project root directory
cd "${BASEDIR}"
format=
native=
ARGS=()

for x in "$@"; do
  case "$x" in
    --format)
      echo "Format source"
      format="process-sources"
    ;;
    --native)
      echo "Build and use native images"
      native=-Dnative
    ;;
    *)
      ARGS+=("$x")
    ;;
  esac
done

if [ ${#ARGS[@]} -eq 0 ]; then
  wrap_exec ./mvnw clean install ${format} ${native}
fi

ACTION="${ARGS[0]}"
unset ARGS[0]

case "$ACTION" in
  images)
    if [ -z "$native" ]; then
      wrap_exec ./mvnw clean package -Dquarkus.container-image.build=true -DskipTests ${ARGS[@]}
    else
      wrap_exec ./mvnw clean package \
        -Dquarkus.container-image.build=true -DskipTests \
        ${native} -Dquarkus.native.container-build=true \
        -pl quarkus-micrometer,quarkus-mpmetrics ${ARGS[@]}
    fi
  ;;
  dc)
    override_dc=
    native_dc=
    if [ -e ./deploy/dc/docker-compose.override.yml ]
    then
      override_dc="-f ./deploy/dc/docker-compose.override.yml"
    fi
    if [ -n "$native" ]
    then
      native_dc="-f ./deploy/dc/docker-compose-native.yml"
    fi
    options="-f ./deploy/dc/docker-compose.yml ${native_dc} ${override_dc}"
    wrap_exec docker-compose $options ${ARGS[@]}
  ;;
  jars)
    wrap_mvnw clean package
    wrap_mvnw package spring-boot:repackage  -pl spring5-micrometer
    if [ -n "$native" ]; then
      wrap_mvnw package -Dnative -pl quarkus-micrometer,quarkus-mpmetrics
    fi
  ;;
  start)
    wrap_launch out.server.spring    java -Dmonster-combat -jar spring5-micrometer/target/mc-spring5-micrometer-0.4.0-exec.jar --server.port=8280
    wrap_launch out.server.quarkus   java -Dmonster-combat -Dquarkus.http.port=8281 -jar quarkus-micrometer/target/quarkus-app/quarkus-run.jar
    wrap_launch out.server.mpmetrics java -Dmonster-combat -Dquarkus.http.port=8282 -jar quarkus-mpmetrics/target/quarkus-app/quarkus-run.jar
    if [ -n "$native" ]; then
      wrap_launch out.server.quarkus-native   ./quarkus-micrometer/target/mc-quarkus-micrometer-0.4.0-runner -Dmonster-combat -Dquarkus.http.port=8283
      wrap_launch out.server.mpmetrics-native ./quarkus-mpmetrics/target/mc-quarkus-mpmetrics-0.4.0-runner -Dmonster-combat -Dquarkus.http.port=8284
    fi
  ;;
  status|list)
    wrap_exec ps -A -o pid,command | grep monster-combat | grep -v grep
  ;;
  mem)
    if top -version 2>/dev/null; then
      echo "linux"
    else
      wrap_exec ps -A -o pid,%cpu,vsz,rss,%mem,command|egrep "MEM|monster-combat"|grep -v grep
    fi
  ;;
  memwatch)
    pids=$(ps -m -o pid,command | awk '{$1=$1};1'| grep monster-combat | grep -v grep | cut -d ' ' -f1)
    if top -version 2>/dev/null; then
      echo "linux"
    else
      wrap_exec top -o pid -r -stats pid,cpu,vsize,mem,command $(printf -- '-pid %s ' ${pids})
    fi
  ;;
  stop)
    pids=$(ps -A -o pid,command | awk '{$1=$1};1' | grep monster-combat | grep -v grep | cut -d ' ' -f1)
    for x in $pids; do echo "stopping $x"; kill $x; done
  ;;
  help)
    usage
  ;;
  *)
    echo "Unknown: $ACTION ${ARGS[@]}"
    usage
  ;;
esac

