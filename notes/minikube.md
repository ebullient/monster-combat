minikube delete && minikube start --kubernetes-version=v1.14.4 --cpus 4 --disk-size 40g --memory 16384 --bootstrapper=kubeadm --extra-config=kubelet.authentication-token-webhook=true --extra-config=kubelet.authorization-mode=Webhook --extra-config=scheduler.address=0.0.0.0 --extra-config=controller-manager.address=0.0.0.0

minikube addons disable metrics-server

cd ~/oss.git/kube-prometheus

$ kubectl create -f manifests/

# It can take a few seconds for the above 'create manifests' command to fully create the following resources, so verify the resources are ready before proceeding.
$ until kubectl get customresourcedefinitions servicemonitors.monitoring.coreos.com ; do date; sleep 1; echo ""; done
$ until kubectl get servicemonitors --all-namespaces ; do date; sleep 1; echo ""; done

$ kubectl apply -f m



kubectl apply kubernetes/dashboard-ingress.yaml
kubectl apply kubernetes/grafana-ingress.yaml
kubectl apply kubernetes/prometheus-ingress.yaml

kubectl create namespace ebullientworks
kubectl apply -f monster-combat.yaml
kubectl apply -f monster-ingress.yaml

kubectl delete namespace ebullientworks

