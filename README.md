# Monster Combat

This Spring Boot 2 application has a few purposes:

a) to teach me, a rookie Dungeon Master, how D&D combat rules work
b) to explore the capabilities of [Micrometer metrics]()
c) to mess with metrics and spring boot applications with Kubernetes, Prometheus, and Grafana.

This application also uses WebFlux (no Tomcat).

Metrics are gathered in `src/main/java/application/battle/BattleMetrics.java`. I kept it all in one place to make it easier for me to fuss around. This approach means I did not use some of the Spring Micrometer annotations, but I felt the trade-off was worth it.

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
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

## General bring-up instructions

1. [Create or retrieve credentials for your cluster](#set-up-a-kubernetes-cluster)

2. Set up custom namespaces (`gameontext` and `ebullientworks`)

    ```bash
    kubectl apply -f deploy/k8s/namespaces.yaml
    ```

3. Set up [kube-prometheus](https://github.com/coreos/kube-prometheus)

    This script wraps all kinds of jsonnet goodness in a container so there is less setup overall:

    ```bash
    ./deploy/k8s/kube-prometheus/build.sh prep     # Once. setup kube-prometheus jsonnet
    # At least one time. Repeat if you change the monsters.jsonnet file
    ./deploy/k8s/kube-prometheus/build.sh generate # Create manifests
    ./deploy/k8s/kube-prometheus/build.sh apply    # Apply configuration to cluster
    ```

    Note there are customizations happening in this step (in `./deploy/k8s/kube-prometheus/monsters.jsonnet`):

    1. We reduce prometheus and alertmanager to single replicas. This is definitely a "fit on a tinier system" move that goes away from resilience.
    2. We instruct prometheus to monitor three additional namespaces: `gameon-system`, `ebullientworks` and `default`. The first is for services from https://gameontext.org, the second is used by this project, and the third is for your own experiments.

4. Once the kube-prometheus manifests have applied cleanly, set up a Prometheus `ServiceMonitor` for Spring applications:

    ```bash
    kubectl apply -f deploy/k8s/spring-prometheus/
    ```

    If you delete/re-apply kube-prometheus metadata, you'll need to reapply this, too, as it is deployed into the `monitoring` namespace. For best results, ensure this is applied, and [spring-prometheus is included in the list of Prometheus targets]() before moving on to the next step.

4. Finally (!!), build and install the application:

    ```bash
    # Choices choices. For minikube and minishift, you may want to share the VM registry
    eval $(minikube docker-env)
    # OR
    eval $(minishift docker-env)

    # Run through all of the sub-projects and build them
    This uses dockerBuild from the jib plugin to create an image
    # in the local docker registry. Feel free to change that up.
    ./buildme.sh

    # Depending on your choices, you may have to do a docker push
    # at this stage, to put fresh images wherever they need to go.

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

# Battles:
# faceoff is 2 monsters
curl http://monsters.192.168.99.100.nip.io/battle/faceoff
# melee is 3-5 monsters
curl http://monsters.192.168.99.100.nip.io/battle/melee
```

Check out the prometheus endpoit to see what metrics are being emitted.

Hopefully, that all worked fine. If it didn't, come find me in the [gameontext slack](https://gameontext.org/slackin) and let me know. Or, ya know, open an issue. That works, too.

---

## Working with Prometheus and Grafana

I am lazy. I dislike the behavior of port forwarding. I defined an ingress for kubernetes-dashboard, prometheus, and grafana:

```bash
kubectl apply -f deploy/lazy/
```

Now you can visit the following in your browser:

* [http://dashboard.192.168.99.100.nip.io](http://dashboard.192.168.99.100.nip.io)
* [http://grafana.192.168.99.100.nip.io](http://grafana.192.168.99.100.nip.io)
* [http://prometheus.192.168.99.100.nip.io](http://prometheus.192.168.99.100.nip.io)

Feel free to adjust these if you aren't using minikube/minishift.

The `runme.sh` script will keep a steady stream of requests hitting an endpoint of your choosing.

---

## Set up a Kubernetes cluster

`kubectl` needs to be able to talk to a Kuberenetes cluster! You may have one already, in which case, all you need to do is make sure `kubectl` can work with it.

* [Minikube](#minikube) -- local development cluster
* [Minishift](#minishift) -- local development cluster (OpenShift 3.x)

### Working with minikube

If you already have a configured minikube instance, skip to step 3.

1. [Install minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)

2. Start Minikube:

    ```bash
    minikube delete
    minikube start --kubernetes-version=v1.14.7 \
    --cpus 4 --disk-size 40g \
    --memory 16384 --bootstrapper=kubeadm \
    --extra-config=kubelet.authentication-token-webhook=true \
    --extra-config=kubelet.authorization-mode=Webhook \
    --extra-config=scheduler.address=0.0.0.0 \
    --extra-config=controller-manager.address=0.0.0.0

    # just in case, but should only have to check once
    minikube addons disable metrics-server
    ```

3. Ensure the `minikube` context is current context for `kubectl`

    ```bash
    kubectl config set-context minikube
    ```

### Working with MiniShift

Coming soon.
