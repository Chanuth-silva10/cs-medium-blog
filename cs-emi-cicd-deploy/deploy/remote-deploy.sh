#!/usr/bin/env bash
# Runs ON THE SERVER, started over SSH by the pipeline. Usage: remote-deploy.sh <image:tag>
# It replaces the running container with the new image, waits for it to be healthy and, if it
# does not become healthy, puts the previous version back and fails.
set -euo pipefail

IMAGE="${1:?usage: remote-deploy.sh <image:tag>}"
NAME="${CONTAINER_NAME:-emi}"
APP_PORT=7550
HOST_PORT="${HOST_PORT:-7550}"
WAIT_SECONDS="${WAIT_SECONDS:-90}"

previous_image="$(docker inspect --format '{{.Config.Image}}' "$NAME" 2>/dev/null || true)"

start_container() {
  # 127.0.0.1: only Nginx on this server can reach the application.
  docker run -d --name "$NAME" --restart unless-stopped \
    -p "127.0.0.1:${HOST_PORT}:${APP_PORT}" "$1" > /dev/null
}

wait_until_healthy() {
  local waited=0 status running
  while [ "$waited" -lt "$WAIT_SECONDS" ]; do
    running="$(docker inspect --format '{{.State.Running}}' "$NAME" 2>/dev/null || echo false)"
    [ "$running" = "true" ] || return 1
    status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$NAME")"
    [ "$status" = "healthy" ] && return 0
    sleep 2
    waited=$((waited + 2))
  done
  return 1
}

remove_container() {
  docker stop --time 30 "$NAME" > /dev/null 2>&1 || true
  docker rm "$NAME" > /dev/null 2>&1 || true
}

echo "Pulling ${IMAGE}"
docker pull "$IMAGE"

echo "Replacing the container ${NAME} (was: ${previous_image:-nothing})"
remove_container
start_container "$IMAGE"

if wait_until_healthy; then
  echo "Deployed ${IMAGE}"
  docker image prune --force > /dev/null
  exit 0
fi

echo "The new version did not become healthy. Last log lines:" >&2
docker logs --tail 20 "$NAME" >&2 || true
remove_container

if [ -n "$previous_image" ]; then
  echo "Putting back ${previous_image}" >&2
  start_container "$previous_image"
  if wait_until_healthy; then
    echo "Rolled back to ${previous_image}" >&2
  else
    echo "The rollback is not healthy either" >&2
  fi
fi
exit 1
