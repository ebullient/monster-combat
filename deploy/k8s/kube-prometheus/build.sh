#!/bin/bash
THIS_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${THIS_DIR}

set -e
set -x
set -o pipefail

if [ $# -lt 1 ]
then
  ACTION=default
else
  ACTION=$1
  shift
fi

MANIFEST_DIR=${THIS_DIR}/target/manifests
VENDOR_DIR=${THIS_DIR}/target/vendor

case "$ACTION" in
  prep)
    mkdir -p $VENDOR_DIR
    mkdir -p $MANIFEST_DIR
    rm -rf $MANIFEST_DIR/*

    # Done once
    docker-compose run --rm jsonnet jb init
    docker-compose run --rm jsonnet jb install github.com/coreos/kube-prometheus/jsonnet/kube-prometheus@release-0.1

    # Make sure dependencies are up to date
    docker-compose run --rm jsonnet jb update
  ;;
  update)
    # Make sure dependencies are up to date
    docker-compose run --rm jsonnet jb update
  ;;
  generate)
    if [ ! -d $VENDOR_DIR ] || [ ! -d $MANIFEST_DIR ]; then
      echo "Run the prep step first"
      exit 1
    fi

    cp monsters.jsonnet target
    # Use the pass-through docker container to generate yaml manifests
    docker-compose run --rm jsonnet /bin/bash -c "
      go get github.com/google/go-jsonnet/cmd/jsonnet && \
      go get github.com/brancz/gojsontoyaml && \
      jsonnet -J vendor -m manifests \
        \"monsters.jsonnet\" | xargs -I{} sh -c 'cat {} | gojsontoyaml > {}.yaml; rm -f {}' -- {}"
  ;;
  apply)
    # Apply the things!
    kubectl apply -f $MANIFEST_DIR

    # Wait until things are up
    until kubectl get customresourcedefinitions servicemonitors.monitoring.coreos.com ; do date; sleep 1; echo ""; done
    until kubectl get servicemonitors --all-namespaces ; do date; sleep 1; echo ""; done

    # Make sure it applies cleanly
    kubectl apply -f $MANIFEST_DIR
  ;;
esac
