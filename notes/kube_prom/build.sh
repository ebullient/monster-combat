#!/bin/bash

set -e
set -x
set -o pipefail

rm -rf manifests
mkdir manifests

# Use the pass-through docker container to generate yaml manifests
docker-compose run --rm jsonnet /bin/bash -c "
  go get github.com/google/go-jsonnet/cmd/jsonnet && \
  go get github.com/brancz/gojsontoyaml && \
  jsonnet -J vendor -m manifests \
    \""${1-example.jsonnet}"\" | xargs -I{} sh -c 'cat {} | gojsontoyaml > {}.yaml; rm -f {}' -- {}"
