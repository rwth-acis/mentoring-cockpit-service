#!/bin/sh
CA_CERT=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
curl -k -X PUT --cacert $CA_CERT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d @mentoring-cockpit-service.json "https://137.226.232.175:6443/apis/apps/v1/namespaces/mztud-test/deployments/mentoring-cockpit-service"
POD_NAME=$(curl -k --cacert $CA_CERT -H "Authorization: Bearer $TOKEN" "https://137.226.232.175:6443/api/v1/namespaces/mztud-test/pods" | jq '.items[] | .metadata.name' | grep mentoring-cockpit-service | sed 's/"//g')
curl -k -X DELETE --cacert $CA_CERT -H "Authorization: Bearer $TOKEN" "https://137.226.232.175:6443/api/v1/namespaces/mztud-test/pods/$POD_NAME"
