#!/usr/bin/env bash
# Runs on YOUR computer. Copies the new JAR to the server and restarts the service.
# Usage: SERVER=deploy@203.0.113.10 SSH_PORT=22 deploy/deploy.sh target/emi.jar
set -euo pipefail

JAR="${1:?usage: deploy.sh <path to the JAR>}"
SERVER="${SERVER:?set SERVER, for example deploy@203.0.113.10}"
SSH_PORT="${SSH_PORT:-22}"
VERSION="$(date +%Y%m%d%H%M%S)"
SSH=(ssh -p "$SSH_PORT" -o BatchMode=yes "$SERVER")

# 1. Upload next to the live version under a unique name. Nothing changes for visitors yet.
scp -P "$SSH_PORT" "$JAR" "$SERVER:/opt/emi/emi-${VERSION}.jar"

# 2. Switch the link, restart, and check the health endpoint. Go back if it does not answer.
"${SSH[@]}" bash -s -- "$VERSION" <<'REMOTE'
set -euo pipefail
cd /opt/emi
new="emi-$1.jar"
old="$(readlink current.jar || true)"
ln -sfn "$new" current.jar
sudo systemctl restart emi
for _ in $(seq 1 30); do
  if curl -fsS http://127.0.0.1:7570/actuator/health > /dev/null 2>&1; then
    echo "Deployed $new"
    ls -1t emi-*.jar | tail -n +4 | xargs -r rm --
    exit 0
  fi
  sleep 2
done
echo "The new version did not answer. Going back to ${old:-nothing}." >&2
if [ -n "$old" ]; then
  ln -sfn "$old" current.jar
  sudo systemctl restart emi
fi
exit 1
REMOTE