#!/usr/bin/env bash
# Backs up one database into a single compressed archive file, checks it, and removes old backups.
#
#   MONGO_URI=mongodb://user:password@host:27017/?authSource=admin scripts/backup.sh <database>
#
# Settings (environment): MONGO_URI (required), BACKUP_DIR (default /backups), KEEP (default 7)
set -euo pipefail

DATABASE="${1:?usage: backup.sh <database>}"
: "${MONGO_URI:?set MONGO_URI}"
BACKUP_DIR="${BACKUP_DIR:-/backups}"
KEEP="${KEEP:-7}"

mkdir -p "$BACKUP_DIR"

# The connection string holds the password. On the command line (--uri) every process of the machine
# can read it with "ps". In a configuration file that only we can read, it cannot.
CONFIG="$(mktemp)"
trap 'rm -f "$CONFIG"' EXIT
chmod 600 "$CONFIG"
printf 'uri: "%s"\n' "$MONGO_URI" > "$CONFIG"

STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
FINAL="${BACKUP_DIR}/${DATABASE}-${STAMP}.archive.gz"
PARTIAL="${FINAL}.partial"

# Write to a name that no restore would ever pick, and rename only when the dump is complete.
if ! mongodump --config "$CONFIG" --db "$DATABASE" --archive="$PARTIAL" --gzip --quiet; then
  rm -f "$PARTIAL"
  echo "Backup of ${DATABASE} FAILED" >&2
  exit 1
fi
mv "$PARTIAL" "$FINAL"

# A checksum lets the restore (or you, a year later) know that the file did not change or break.
(cd "$BACKUP_DIR" && sha256sum "$(basename "$FINAL")" > "$(basename "$FINAL").sha256")

# Keep the newest $KEEP backups of this database, delete the others.
ls -1t "${BACKUP_DIR}/${DATABASE}"-*.archive.gz 2>/dev/null | tail -n +"$((KEEP + 1))" | while read -r old; do
  rm -f "$old" "${old}.sha256"
  echo "Removed old backup $(basename "$old")"
done

echo "Backup written: $(basename "$FINAL") ($(stat -c %s "$FINAL") bytes)"
