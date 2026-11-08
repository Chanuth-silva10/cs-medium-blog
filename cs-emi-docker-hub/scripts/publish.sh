#!/usr/bin/env bash
# Builds the image and pushes it to a registry (Docker Hub by default).
# Usage: DOCKERHUB_USERNAME=chanuthsilva DOCKERHUB_TOKEN=... scripts/publish.sh 1.0.0
set -euo pipefail

VERSION="${1:?usage: publish.sh <version>, for example 1.0.0}"
USERNAME="${DOCKERHUB_USERNAME:?set DOCKERHUB_USERNAME}"
TOKEN="${DOCKERHUB_TOKEN:?set DOCKERHUB_TOKEN (an access token, not your password)}"
# Empty means Docker Hub. Set it to another registry to publish there, for example localhost:5001.
REGISTRY="${REGISTRY:-}"
REPOSITORY="${REPOSITORY:-cs-emi}"

if [ -n "$REGISTRY" ]; then
  IMAGE="${REGISTRY}/${USERNAME}/${REPOSITORY}"
else
  IMAGE="${USERNAME}/${REPOSITORY}"
fi

# --password-stdin keeps the token out of the process list and out of the shell history.
printf '%s' "$TOKEN" | docker login ${REGISTRY:+"$REGISTRY"} --username "$USERNAME" --password-stdin

docker build --tag "${IMAGE}:${VERSION}" .
# The same image under a second, moving tag. The version tag never changes, "latest" always does.
docker tag "${IMAGE}:${VERSION}" "${IMAGE}:latest"

docker push "${IMAGE}:${VERSION}"
docker push "${IMAGE}:latest"

docker logout ${REGISTRY:+"$REGISTRY"}
echo "Published ${IMAGE}:${VERSION}"
