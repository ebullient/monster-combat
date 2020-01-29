# Brain dump - kubernetes

## Minikube

```
minikube delete
minikube start --kubernetes-version=v1.14.7 \
  --cpus 4 --disk-size 40g \
  --memory 16384 --bootstrapper=kubeadm \
  --extra-config=kubelet.authentication-token-webhook=true \
  --extra-config=kubelet.authorization-mode=Webhook \
  --extra-config=scheduler.address=0.0.0.0 \
  --extra-config=controller-manager.address=0.0.0.0

# just in case. Should only have to check once
minikube addons disable metrics-server
```

## Namespaces

Required namespaces, and Spring application endpoint detection (`ServiceMonitor`):

```bash
kubectl apply -f namespaces.yaml
```

## kube-prometheus

Uses jsonnet, must be customized to add additional namespaces
--> Docker compose file wraps all the fiddly bits for pass-through
--> LAZY: use build.sh for all

```bash
# Done once
./kube-prometheus/build.sh prep

# Generate the manifests
./kube-prometheus/build.sh generate

# Apply the things!
./kube-prometheus/build.sh apply
```

## Other kube things

Spring application endpoint detection (`ServiceMonitor`):

```bash
kubectl apply -f spring-prometheus/
```

Because I am lazy and hate remembering to proxy things. Also, I am lazy with namespaces

```bash
kubectl apply -f lazy/
```

## Finally the yaml for deploying the application to kube

Build the application first. Make sure the images are available in the cluster's image registry.

```bash
kubectl apply -f monsters/
```

## Run stuff

```bash
../../runme.sh
```

## Tearing things down

```bash
kubectl delete namespace ebullientworks
```
