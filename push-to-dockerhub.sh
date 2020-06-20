#!/bin/sh
CA_CERT=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)
DOCKER_DATA=$(curl -k --cacert $CA_CERT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" "https://137.226.232.175:6443/api/v1/namespaces/mztud-test/secrets/docker-acis" | jq .data)
DOCKER_PW=$(echo $DOCKER_DATA | jq -r .DOCKER_ACIS_PW | base64 -d)
DOCKER_USER=$(echo $DOCKER_DATA | jq -r .DOCKER_ACIS_USER | base64 -d)

docker login -u "$DOCKER_USER" -p "$DOCKER_PW" https://index.docker.io/v1/
docker push "rwthacis/mentoring-cockpit-service:develop"
