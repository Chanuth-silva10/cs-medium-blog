#!/usr/bin/env bash
# Prints, for every collection of two databases, the number of documents and of indexes, then says
# if they are the same.
#
#   scripts/compare.sh <uri A> <database A> <uri B> <database B>
set -euo pipefail

URI_A="${1:?usage: compare.sh <uri A> <database A> <uri B> <database B>}"
DB_A="${2:?}"
URI_B="${3:?}"
DB_B="${4:?}"

summary() {
  mongosh "$1" --quiet --eval "
    const database = db.getSiblingDB('$2');
    database.getCollectionNames().sort().forEach((name) => {
      print(name + ' documents=' + database[name].countDocuments() + ' indexes=' + database[name].getIndexes().length);
    });"
}

A="$(summary "$URI_A" "$DB_A")"
B="$(summary "$URI_B" "$DB_B")"
echo "--- $DB_A"; echo "$A"
echo "--- $DB_B"; echo "$B"
if [ "$A" = "$B" ]; then
  echo "SAME: collections, document counts and index counts match"
else
  echo "DIFFERENT" >&2
  exit 1
fi
