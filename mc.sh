#!/bin/bash
BASEDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
DC_DIR="$BASEDIR/deploy/dc"

wrap() {
  echo "
> $@"
  $@
}


# Ensure we're executing from project root directory
cd "${BASEDIR}"
format=
native=
ARGS=()

for x in "$@"; do
  case "$x" in
    format)
      echo "Format source"
      format="process-sources"
    ;;
    native)
      echo "Build and use native images"
      native=-Dnative
    ;;
    *)
      ARGS+=("$x")
    ;;
  esac
done

if [ ${#ARGS[@]} -eq 0 ]; then
  wrap ${BASEDIR}/mvnw clean install ${format} ${native}
  exit
fi

ACTION="${ARGS[0]}"
unset ARGS[0]

case "$ACTION" in
  pkg-image)
    if [ -z "$native" ]; then
      wrap ${BASEDIR}/mvnw clean package \
        -Dquarkus.container-image.build=true -DskipTests \
        -pl quarkus-micrometer,quarkus-mpmetrics ${ARGS[@]}
    else
      wrap ${BASEDIR}/mvnw clean package \
        -Dquarkus.container-image.build=true -DskipTests \
        -Dnative -Dquarkus.native.container-build=true \
        -pl quarkus-micrometer,quarkus-mpmetrics ${ARGS[@]}
    fi
  ;;
  dc)
    override_dc=
    if [ -e $DC_DIR/docker-compose.override.yml ]
    then
      override_dc="-f $DC_DIR/docker-compose.override.yml"
    fi
    native_dc=
    if [ -n "$native" ]
    then
      native_dc="-f $DC_DIR/docker-compose-native.yml"
    fi
    options="-f $DC_DIR/docker-compose.yml ${native_dc} ${override_dc}"
    wrap docker-compose $options ${ARGS[@]}
  ;;
  *)
    echo "Unknown: $ACTION ${ARGS[@]}"
  ;;
esac

