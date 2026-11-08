#!/usr/bin/env bash
# Prints who is primary and who is secondary. Works from any member that is running.
set -euo pipefail

for member in "mongo1 27021" "mongo2 27022" "mongo3 27023"; do
  set -- $member
  if docker exec "$1" mongosh --port "$2" --quiet --eval \
    "rs.status().members.map(m => m.name + ' ' + m.stateStr + (m.health ? '' : ' (down)')).join('\n')" 2>/dev/null; then
    exit 0
  fi
done
echo "No member answers. Is the replica set running? (docker compose up -d --wait)" >&2
exit 1
