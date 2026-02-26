#!/bin/bash
set -e

NAMESPACE=scraper

echo "Applying namespace..."
kubectl apply -f namespace.yaml

echo "Applying config and secrets..."
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml

echo "Deploying infrastructure..."
kubectl apply -f infra/

echo "Deploying services..."
kubectl apply -f scraper-api/
kubectl apply -f scraper-scheduler/
kubectl apply -f scraper-worker/
kubectl apply -f results-processor/

echo "Deployment complete. Checking rollout status..."
kubectl rollout status deployment/scraper-api -n $NAMESPACE
kubectl rollout status deployment/scraper-worker -n $NAMESPACE
kubectl rollout status deployment/scraper-scheduler -n $NAMESPACE
kubectl rollout status deployment/results-processor -n $NAMESPACE

echo "All services deployed successfully."

echo "Deploying observability stack..."
kubectl apply -f ../observability/prometheus/
kubectl apply -f ../observability/grafana/
kubectl apply -f ../observability/loki/
echo "Grafana available at http://localhost:30300 (admin/admin)"