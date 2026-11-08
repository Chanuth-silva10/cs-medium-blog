#!/usr/bin/env bash
# Restores a backup archive made by backup.sh, into the same database name or into another one.
#
#   MONGO_URI=... scripts/restore.sh <archive file> [target database] [--drop]
#
# --drop replaces the collections that already exist. Without it, documents that already exist are
# reported as duplicates and left untouched.
set -euo pipefail

ARCHIVE="${1:?usage: restore.sh <archive file> [target database] [--drop]}"
TARGET_DB="${2:-}"
DROP=""
for argument in "$@"; do
  [ "$argument" = "--drop" ] && DROP="--drop"
done
[ "$TARGET_DB" = "--drop" ] && TARGET_DB=""
: "${MONGO_URI:?set MONGO_URI}"

# The database name is the start of the file name: <database>-<timestamp>.archive.gz
SOURCE_DB="$(basename "$ARCHIVE" | sed -E 's/-[0-9]{8}T[0-9]{6}Z\.archive\.gz$//')"
TARGET_DB="${TARGET_DB:-$SOURCE_DB}"

if [ -f "${ARCHIVE}.sha256" ]; then
  (cd "$(dirname "$ARCHIVE")" && sha256sum --check --status "$(basename "$ARCHIVE").sha256") \
    || { echo "The checksum does not match: the archive is damaged. Nothing was restored." >&2; exit 1; }
  echo "Checksum OK"
else
  echo "Warning: no checksum file next to the archive" >&2
fi

CONFIG="$(mktemp)"
trap 'rm -f "$CONFIG"' EXIT
chmod 600 "$CONFIG"
printf 'uri: "%s"\n' "$MONGO_URI" > "$CONFIG"

# --nsFrom/--nsTo rename the database while restoring: "prolang.*" becomes "prolang_copy.*".
mongorestore --config "$CONFIG" --archive="$ARCHIVE" --gzip \
  --nsInclude "${SOURCE_DB}.*" --nsFrom "${SOURCE_DB}.*" --nsTo "${TARGET_DB}.*" $DROP
echo "Restored ${SOURCE_DB} into ${TARGET_DB}"
