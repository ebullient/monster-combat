#!/bin/bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

wrap_exec() {
  echo "
> $@"
  exec $@
}

usage() {
      echo "
mc.sh [--format|--native] [images|dc|help]

  format
    Flag that triggers formatting of source code.

  native
    Flag that triggers use/inclusion of native image

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
  help)
    usage
  ;;
  *)
    echo "Unknown: $ACTION ${ARGS[@]}"
    usage
  ;;
esac

