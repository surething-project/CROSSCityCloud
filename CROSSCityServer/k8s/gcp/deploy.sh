#!/bin/sh

# Elevate to the Cluster Admin Role
kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=$(gcloud config get-value core/account)

# Create and Apply the Resources Required for PostgreSQL
kubectl apply -f psql-secret.yaml
kubectl create configmap cross-psql-schema --from-file=../../src/main/resources/database/1-schema.sql
kubectl create configmap cross-psql-populate --from-file=../../src/main/resources/database/2-populate.sql
kubectl apply -f psql-pv.yaml
kubectl apply -f psql.yaml

# Create and Apply the Resources Required for CROSS Server
kubectl apply -f cross-secret.yaml
kubectl apply -f cross-config.yaml
kubectl create secret generic cross-key --from-file ./key.json
kubectl apply -f cross.yaml

# Create and Apply the External DNS
kubectl apply -f external-dns.yaml

# Create and Apply the Nginx Ingress Controller
kubectl apply -f nginx-ingress.yaml

echo "Waiting for the NGINX ingress controller to be created"

sleep 60

# Create and Apply the Cert-Manager 
kubectl apply -f cert-manager.yaml

# Create and Apply the Resources Required for our CA
kubectl apply -f ca-key-pair.yaml
sleep 20
kubectl apply -f ca-issuer.yaml

# Create Prometheus stack resources from helm chart including Prometheus, Grafana and the Operator 
# (https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)  
helm install main prometheus-community/kube-prometheus-stack

sleep 10

# Create and Apply the Ingress Controller Rule
kubectl apply -f cross-ingress.yaml
