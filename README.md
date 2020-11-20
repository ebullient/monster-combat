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

## Getting started

Obtain the source for this repository:

* HTTPS: git clone https://github.com/ebullient/monster-combat.git
* SSH: git clone git@github.com:ebullient/monster-combat.git

Start with:

```bash
cd monster-combat                  # cd into the project directory
export MONSTER_DIR=${PWD}          # for future reference
```

### Prerequisites

* [Docker](https://docs.docker.com/install/)

## Lazy bones: quick and dirty with docker-compose

This application is all about application metrics. The surrounding environment doesn't matter much.
If you're lazy, or on a constrained system, docker-compose will work fine to start all the bits.
Note: I'm lazy, so this is the method I use the most often.

```bash
./buildme.sh
# buildme.sh creates the following output directories to ensure host user ownership:
# mkdir -p deploy/dc/target/prometheus deploy/dc/target/grafana

cd deploy/dc
docker-compose up -d
```

You should be able to do the following and get something interesting in return:

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
curl http://127.0.0.1:8281/metrics         # micrometer metrics endpoint
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

The `runme.sh` script will keep a steady stream of requests hitting an endpoint of your choosing.

Hopefully, that all worked fine. If it didn't, come find me in the [gameontext slack](https://gameontext.org/slackin) and let me know.
Or open an issue. That works, too.

### Prometheus and Grafana with docker-compose

The `${MONSTER_DIR}/deploy/dc/config` directory contains configuration for Prometheus and Grafana when run with docker-compose.
The config directory is bind-mounted into both containers. The docker-compose configuration also creates a bind mount to
service-specific subdirectories under `target/data` for output.

The config directory conains the following files:

* `grafana.ini` configures grafana
* `grafana-*.json` are importable grafana dashboards
* `prometheus.yml` defines jobs for spring and quarkus metrics, and declares `prometheus.rules.yaml`
* `prometheus.rules.yaml` defines reporting rules for prometheus that create additional time series to pre-aggregate chattier metrics.

To reset prometheus and grafana (tossing all data):

```bash
docker-compose stop prom grafana
docker-compose rm prom grafana
rm -rf ${MONSTER_DIR}/deploy/dc/target/data/prometheus/*  ${MONSTER_DIR}/deploy/dc/target/data/grafana/*
docker-compose up -d prom grafana
```

Note: `${MONSTER_DIR}/deploy/dc/target/data/prometheus/` and `${MONSTER_DIR}/deploy/dc/target/data/grafana/` must be owned
by the host user. If you delete the directories by accident, recreate them manually before using docker-compose to start
the services again (as it will create the missing directories for you, and those will be owned by root, which will cause
permission issues for services running as the host user).

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

    1. We reduce prometheus and alertmanager to single replicas. This is definitely a "fit on a tinier system" move that goes away from resilience.
    2. We instruct prometheus to monitor three additional namespaces: `gameon-system`, `ebullientworks` and `default`. The first is for services from https://gameontext.org, the second is used by this project, and the third is for your own experiments.

4. Once the kube-prometheus manifests have applied cleanly, set up a Prometheus `ServiceMonitor` for our applications:

    ```bash
    kubectl apply -f deploy/k8s/service-monitor/
    ```

    If you delete/re-apply kube-prometheus metadata, you'll need to reapply this, too, as it is deployed into
    the `monitoring` namespace. For best results, ensure this is applied, and both `mc-quarkus-prometheus` and
    `mc-spring-prometheus` are included in the list of Prometheus targets before moving on to the next step.

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

The `runme.sh` script will keep a steady stream of requests hitting an endpoint of your choosing.

Hopefully, that all worked fine. If it didn't, come find me in the [gameontext slack](https://gameontext.org/slackin) and let me know. Or open an issue. That works, too.

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
    minikube addons disable metrics-server
    minikube addons enable ingress
    minikube start --kubernetes-version=v1.19.4 \
    --cpus 4 --disk-size 40g \
    --memory 16384 --bootstrapper=kubeadm \
    --extra-config=kubelet.authentication-token-webhook=true \
    --extra-config=kubelet.authorization-mode=Webhook \
    --extra-config=scheduler.address=0.0.0.0 \
    --extra-config=controller-manager.address=0.0.0.0
    ```

3. Ensure the `minikube` context is current context for `kubectl`

    ```bash
    kubectl config set-context minikube
    ```

### Working with CodeReady Containers

Coming soon.
