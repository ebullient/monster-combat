# Brain dump

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

# just in case, but should only have to check once
minikube addons disable metrics-server
```

## kube-prometheus

Uses jsonnet
must be customized to add additional namespaces
--> Docker compose file wraps all the fiddly bits for pass-through

```
cd notes/kube_prom

# Done once
docker-compose run --rm jsonnet jb init
docker-compose run --rm jsonnet jb install github.com/coreos/kube-prometheus/jsonnet/kube-prometheus@release-0.1

# Make sure dependencies are up to date
docker-compose run --rm jsonnet jb update

# Generate kube manifests
./build.sh monsters.jsonnet

# Apply the things!
kubectl apply -f manifests/

# Wait until things are up
until kubectl get customresourcedefinitions servicemonitors.monitoring.coreos.com ; do date; sleep 1; echo ""; done
until kubectl get servicemonitors --all-namespaces ; do date; sleep 1; echo ""; done

# Make sure it applies cleanly
kubectl apply -f manifests/
```

## Other kube things

Because I am lazy and hate remembering to proxy things. Also, I am lazy with namespaces

```
kubectl apply -f notes/lazy/
```

## Finally the yaml for deploying this to kube

```

kubectl apply -f deploy/
```

## Run stuff

./notes/runme.sh

## Tearing things down:

kubectl delete namespace ebullientworks

