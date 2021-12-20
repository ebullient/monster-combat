# Monster Combat

TLDR; This application had a few purposes:

1. to teach me, a rookie Dungeon Master, how D&D combat rules work (specifically, [D&D 5e](https://www.dndbeyond.com/sources/basic-rules/combat))
2. to explore usage and capabilities of metrics libraries, starting with [Micrometer](https://micrometer.io)
3. to mess with metrics and spring boot applications with Kubernetes, Prometheus, and Grafana.

You can read more here [Monsters in combat: exploring application metrics with D&D](https://jaxenter.com/metrics-dnd-173311.html)

Additional Notes:

* The Spring application also uses WebFlux (no Tomcat).
* The Quarkus application uses the micrometer core library

One injectable class, `CombatMetrics.java` in the core library, defines metrics gathered
using micrometer. This class is used by both the Spring and Quarkus-micrometer applications
to collect custom metrics. I wanted metrics definitions to be easy to find, and easy to change.
This choice means I'm not making extensive use of annotation-based configurations, but I
think the result is clear and concise, and much less invasive than annotations would have been.

> A quick note about `quarkus:dev`
>
> Both Quarkus projects use variables derived from git to populate image attributes. Quarkus dev mode will complain if these attributes aren't set.
>
> To set git project attributes, invoke the git plugin along with the dev mode plugin:
>
> ```bash
> mvn pl.project13.maven:git-commit-id-plugin:revision quarkus:dev
> ```
>

## Prerequisites

* [Docker](https://docs.docker.com/install/)
* Java 11

## Getting started

Obtain the source for this repository:

* HTTPS: git clone https://github.com/ebullient/monster-combat.git
* SSH: git clone git@github.com:ebullient/monster-combat.git

Start with:

```bash
cd monster-combat           # cd into the project directory
export MONSTER_DIR=${PWD}   # for future reference
```

Get your system up and running using either

* [Docker compose](#lazy-bones-quick-and-dirty-with-docker-compose) or
* [Kubernetes](#general-bring-up-instructions-for-kubernetes)

Once you have your system configured and running, the `runme.sh` script will keep a steady stream of requests hitting an
endpoint of your choosing.

Hopefully, it will all work fine. If it doesn't, come find me in the [gameontext slack](https://gameontext.org/slackin)
and let me know. Or open an issue. That works, too.

## Lazy bones: quick and dirty with docker-compose

This application is all about application metrics. The surrounding environment doesn't matter much.
If you're lazy, or on a constrained system, docker-compose will work fine to start all the bits.
Note: I'm lazy, so this is the method I use the most often.

See below for notes on adding native images to the mix:

```bash
# build images
./mvnw install

# go to docker-compose directory
cd deploy/dc

# start all services (prom, grafana, spring, quarkus, quarkus-mpmetrics)
docker-compose up -d
```

Alternately, use `mc.sh` to manage some of these operations for you:

```bash
# build & package submodules
./mc.sh

# start services using docker compose
./mc.sh dc up -d
```

The `mc.sh` script looks for some flags (like `native` or `format`) to add options to maven commands,
but otherwise hands all remaining command line arguments to invoked commands.
In the case of `dc`, `mc.sh` will execute the docker-compose command with explicitly specified
docker-compose files, which can save a lot of typing once you add native images to the mix.

You should then be able to do the following and get something interesting in return:

```bash
# Spring:

curl http://127.0.0.1:8280/
curl http://127.0.0.1:8280/actuator/metrics
curl http://127.0.0.1:8280/actuator/prometheus
curl http://127.0.0.1:8280/combat/faceoff  # 2 monsters
curl http://127.0.0.1:8280/combat/melee    # 3-6 monsters
curl http://127.0.0.1:8280/combat/any      # 2-6 monsters

# Quarkus

curl http://127.0.0.1:8281/
curl http://127.0.0.1:8281/metrics         # micrometer & prometheus
curl http://127.0.0.1:8281/combat/faceoff  # 2 monsters
curl http://127.0.0.1:8281/combat/melee    # 3-6 monsters
curl http://127.0.0.1:8281/combat/any      # 2-6 monsters

# Quarkus with MP Metrics

curl http://127.0.0.1:8282/
curl http://127.0.0.1:8282/metrics         # MP metrics endpoint
curl http://127.0.0.1:8282/combat/faceoff  # 2 monsters
curl http://127.0.0.1:8282/combat/melee    # 3-6 monsters
curl http://127.0.0.1:8282/combat/any      # 2-6 monsters

```

Check out the prometheus dashboard (http://127.0.0.1:9090) to see emitted metrics.
You can import pre-created dashboards (see below) to visualize collected metrics
in grafana (http://127.0.0.1:3000, admin|admin is default username/password). When
configuring the Prometheus datasource in Graphana, use the docker-compose service
name as the hostname: `http://prometheus:9090`.

### Including native images

If you are using linux, building and testing native images is straightforward,
but if you are using windows or mac, we need to separate the steps a bit, as the
native image needs to be built with a container, and that will overwrite the
OS-native image used for tests.

```bash
# build and test native quarkus images (with GraalVM or Mandrel)
# this produces an OS-specific binary used for unit tests
./mvnw install -Dnative -pl quarkus-micrometer,quarkus-mpmetrics

# On Windows and Mac, perform the following step to create the native
# images using a container and skipping tests
./mvnw clean package -Dnative \
  -Dquarkus.container-image.build=true -DskipTests \
  -Dquarkus.native.container-build=true \
  -pl quarkus-micrometer,quarkus-mpmetrics
```

Use an additional docker compose file to start native images.
Append docker-compose.override.yml to the list of files if necessary.

```bash
docker-compose -f docker-compose.yml -f docker-compose-native.yml up -d
```

Alternately, use `mc.sh` to manage some of these operations for you:

```bash
# build/test quarkus in native mode
./mc.sh native
# create native quarkus images
./mc.sh native pkg-image
# start all services (including non-native) using docker compose
./mc.sh native dc up -d
```

### Prometheus and Grafana with docker-compose

The `${MONSTER_DIR}/deploy/dc/config` directory contains configuration for Prometheus and Grafana when run with docker-compose.
The config directory is bind-mounted into both containers. The docker-compose configuration also creates a bind mount to
service-specific subdirectories under `${MONSTER_DIR}/deploy/dc/target/data` for output.

The config directory conains the following files:

* `grafana.ini` configures grafana
* `grafana-*.json` are importable grafana dashboards
* `prometheus.yml` defines jobs for spring and quarkus metrics, and declares `prometheus.rules.yaml`
* `prometheus.rules.yaml` defines reporting rules for prometheus that create additional time series to pre-aggregate chattier metrics.

To reset prometheus and grafana (tossing all data):

```bash
# From ${MONSTER_DIR}/deploy/dc directory:
docker-compose stop prom grafana
docker-compose rm prom grafana
# Remove data for prometheus and grafana
rm -rf ./target/data/prometheus/*  ./target/data/grafana/*
# restart
docker-compose up -d prom grafana
```

Note: the prometheus and grafana data directories must be owned
by the host user. If you delete the directories by accident, recreate them manually before using docker-compose to start
the services again (as it will create the missing directories for you, and those will be owned by root, which will cause
permission issues for services running as the host user).

### Overlaying runtime container configuration

1. Copy a configuration file, e.g. copy `quarkus-micrometer/src/main/resources/application.properties` to
`${MONSTER_DIR}/deploy/dc/target/mc-quarkus-micrometer.properties`

2. Create an override file, `${MONSTER_DIR}/deploy/dc/docker-compose.override.yml`, that mounts this file as a volume, replacing
   the configuration file in the image:

    ```yaml
    version: '3.7'
    services:

      quarkus:
        volumes:
        - './target/mc-quarkus-micrometer.properties:/app/resources/application.properties'
    ```

## General bring-up instructions for Kubernetes

[Initialize your cluster](#set-up-a-kubernetes-cluster)

1. [Create or retrieve credentials for your cluster](#set-up-a-kubernetes-cluster)

2. Set up custom namespaces (`gameontext` and `ebullientworks`)

    ```bash
    kubectl apply -f deploy/k8s/namespaces.yaml
    ```

3. Set up [kube-prometheus](https://github.com/coreos/kube-prometheus)

    This script wraps all kinds of jsonnet goodness in a container so there is less setup over-all:

    ```bash
    ./deploy/k8s/kube-prometheus/build.sh prep     # Once. setup kube-prometheus jsonnet
    # At least one time. Repeat if you change the monsters.jsonnet file
    ./deploy/k8s/kube-prometheus/build.sh generate # Create manifests
    ./deploy/k8s/kube-prometheus/build.sh apply    # Apply configuration to cluster
    ```

    Note there are customizations happening (in `./deploy/k8s/kube-prometheus/monsters.jsonnet`):

    1. We reduce prometheus and alertmanager to single replicas. This is definitely a "fit on a tinier system" move that
       goes away from resilience.
    2. We instruct prometheus to monitor three additional namespaces: `gameon-system`, `ebullientworks` and `default`.
       The first is for services from https://gameontext.org, the second is used by this project, and the third is for
       your own experiments.

4. Create an ingress for prometheus and grafana

    ```bash
    kubectl apply -f deploy/k8s/ingress/monitoring-ingress.yaml
    echo "Use the following urls for
    Prometheus: http://prometheus.$(minikube ip).nip.io
    Grafana dashboard: http://grafana.$(minikube ip).nip.io"
    ```

4. Once the kube-prometheus manifests have applied cleanly, set up a Prometheus `ServiceMonitor` for our applications:

    ```bash
    kubectl apply -f deploy/k8s/service-monitor/
    ```

    If you delete/re-apply kube-prometheus metadata, you'll need to reapply this, too, as it is deployed into
    the `monitoring` namespace.

    For best results, ensure this is applied, and both `mc-quarkus-prometheus` and `mc-spring-prometheus` are
    included in the list of Prometheus targets before moving on to the next step.

    ```bash
    echo Visit http://prometheus.$(minikube ip).nip.io/targets
    ```

4. Finally (!!), build and install the application:

    ```bash
    # Choices choices. For minikube, you may want to share the VM registry
    eval $(minikube docker-env)

    # Run through all of the sub-projects and build them
    # in the local docker registry. Feel free to change that up.

    ./mvnw install

    # Depending on your choices, you may have to do a docker push
    # to put fresh images wherever they need to go.

    # Now deploy application metadata (service, deployment, ingress)
    # Verify that the ingress definition will work for your kubernetes cluster

    kubectl apply -f deploy/k8s/monsters/
    kubectl apply -f deploy/k8s/ingress/monster-ingress.yaml
    echo "
    Spring with Micrometer: http://spring.$(minikube ip).nip.io
    Quarkus with Micrometer: http://quarkus.$(minikube ip).nip.io
    Quarkus with MP Metrics: http://mpmetrics.$(minikube ip).nip.io"
    ```

So, after all of that, you should be able to do the following and get something interesting in return:

```bash
# This assumes minikube and/or minishift, with the configured ingress URL
curl http://monsters.192.168.99.100.nip.io/
curl http://monsters.192.168.99.100.nip.io/actuator/metrics
curl http://monsters.192.168.99.100.nip.io/actuator/prometheus

# Encounters:
# faceoff is 2 monsters
curl http://monsters.192.168.99.100.nip.io/combat/faceoff
# melee is 3-6 monsters
curl http://monsters.192.168.99.100.nip.io/combat/melee
# any is random from 2-6
curl http://monsters.192.168.99.100.nip.io/combat/any
```

Check out the prometheus endpoint to see what metrics are being emitted.

### Messing around with application configuration using ConfigMaps

We'll use Quarkus Micrometer for this example.

1. Let's start by creating a new ConfigMap for application.properties that specifies
   runtime configuration attributes, e.g. `deploy/k8s/config/mc-quarkus-micrometer-config.yaml`

    ```yaml
    apiVersion: v1
    kind: ConfigMap
    metadata:
      name: mc-quarkus-micrometer-config
      namespace: ebullientworks
    data:
      application.properties: |+
        quarkus.http.port=8080
    ```

2. Update the appropriate deployment definition to reference the volume, e.g. `deploy/k8s/monsters/quarkus-micrometer.yaml`:

    ```yaml
       spec:
         volumes:
           - name: properties-volume
             configMap:
               name: mc-quarkus-micrometer-config
         containers:
         - image: ebullient/mc-quarkus-micrometer:latest-jvm
           imagePullPolicy: IfNotPresent
           name: mc-quarkus-micrometer
           volumeMounts:
           - name: properties-volume
             mountPath: /app/resources/mc-quarkus-micrometer.properties
      ...
    ```

3. Create the ConfigMap and update your deployment

    ```bash
    kubectl apply -f deploy/k8s/config/mc-quarkus-micrometer-config.yaml
    kubectl apply -f deploy/k8s/monsters/quarkus-micrometer.yml
    ```


---

### Set up a Kubernetes cluster

`kubectl` needs to be able to talk to a Kuberenetes cluster! You may have one already, in which case, all you need to do
is make sure `kubectl` can work with it.

* [Minikube](#working-with-minikube) -- local development cluster
* [CodeReady Containers](#working-with-codeready-containers) -- local development cluster (OpenShift 3.x)

### Working with minikube

If you already have a configured minikube instance, skip to step 3.

1. [Install minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)

2. Start Minikube:

    ```bash
    minikube delete
    minikube start --kubernetes-version=v1.19.4 \
    --cpus 4 --disk-size 40g \
    --memory 16384 --bootstrapper=kubeadm \
    --extra-config=kubelet.authentication-token-webhook=true \
    --extra-config=kubelet.authorization-mode=Webhook \
    --extra-config=scheduler.address=0.0.0.0 \
    --extra-config=controller-manager.address=0.0.0.0
    minikube addons disable metrics-server
    minikube addons enable ingress
    ```

3. Ensure the `minikube` context is current context for `kubectl`

    ```bash
    kubectl config set-context minikube

    # ensure ingress is working
    curl -v --raw http://$(minikube ip)/healthz
    ```

4. Update the ingress for your cluster to match the IP

    ```bash
    ./mvnw -Dcluster.ip=$(minikube ip) -Dminikube install -pl deploy/k8s
    ```

### Working with CodeReady Containers

Coming soon.
