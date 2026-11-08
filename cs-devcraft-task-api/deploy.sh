#!/usr/bin/env bash
# Deploys one Docker image to the server over SSH. Everything comes from environment variables
# so the same script works from the GitHub Actions runner and from your own terminal.
set -euo pipefail

: "${SERVER_HOST:?SERVER_HOST is required}"
: "${SERVER_USER:?SERVER_USER is required}"
: "${KEY_FILE:?KEY_FILE is required}"
: "${KNOWN_HOSTS_FILE:?KNOWN_HOSTS_FILE is required}"
: "${IMAGE:?IMAGE is required}"
SERVER_PORT="${SERVER_PORT:-22}"

start=$(date +"%s")

# StrictHostKeyChecking=yes makes SSH refuse any server whose key is not in KNOWN_HOSTS_FILE,
# which is what protects the deployment from a man-in-the-middle.
# The commands between ENDSSH markers run on the server; IMAGE is handed over as a variable.
ssh -p "${SERVER_PORT}" -i "${KEY_FILE}" \
  -o StrictHostKeyChecking=yes -o UserKnownHostsFile="${KNOWN_HOSTS_FILE}" \
  "${SERVER_USER}@${SERVER_HOST}" "IMAGE='${IMAGE}' bash -s" << 'ENDSSH'
set -euo pipefail
CONTAINER_NAME=devcraft-task-api

docker pull "$IMAGE"

# Replace the old container if there is one.
docker rm -f "$CONTAINER_NAME" > /dev/null 2>&1 || true

docker run -d --restart unless-stopped -p 4500:4500 \
  --env-file /opt/devcraft/.env --name "$CONTAINER_NAME" "$IMAGE"

# A running container is not proof of a working app, so wait for the API to answer.
for attempt in $(seq 1 15); do
  if curl -fsS http://localhost:4500/ > /dev/null; then
    echo "The API is answering."
    exit 0
  fi
  sleep 2
done

echo "The API did not answer in time. Last logs:" >&2
docker logs --tail 50 "$CONTAINER_NAME" >&2
exit 1
ENDSSH

end=$(date +"%s")
echo "Deployed ${IMAGE} in $((end - start))s"
