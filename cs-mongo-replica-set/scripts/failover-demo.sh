#!/usr/bin/env bash
# Stops the primary, shows the election, writes through the replica set, brings the old primary back.
set -euo pipefail

NETWORK="devcraft-mongo-rs_mongors"
URI="mongodb://mongo1:27021,mongo2:27022,mongo3:27023/shop?replicaSet=dbrs"

# Runs mongosh in a throw-away container on the same Docker network, using the full replica set address.
run_client() {
  docker run --rm --network "$NETWORK" mongo:7.0.14 mongosh "$URI" --quiet --eval "$1"
}

echo "== Before"
bash "$(dirname "$0")/status.sh"

echo "== Stop mongo1 (the primary)"
docker stop mongo1 > /dev/null

# The other members notice after the election timeout (10 seconds by default).
for _ in $(seq 1 30); do
  primary="$(run_client "db.hello().primary" 2>/dev/null || true)"
  case "$primary" in mongo2*|mongo3*) break ;; esac
  sleep 2
done
echo "New primary: ${primary}"

echo "== Write while mongo1 is down"
run_client "db.orders.insertOne({ item: 'monitor', qty: 1 }, { writeConcern: { w: 'majority' } }).acknowledged"

echo "== Start mongo1 again"
docker start mongo1 > /dev/null
sleep 20
bash "$(dirname "$0")/status.sh"
echo "== Orders seen after the failover"
run_client "db.orders.countDocuments()"
