# CS Mongo Backup Toolkit
Export, import, back up and restore a MongoDB database with the MongoDB Database Tools, plus three small scripts that make backups safe and checkable. A lab with two MongoDB servers is included, so you can try everything without touching real data. Part of the DevCraft series by CS Solution (Chapter 39).
## The lab
```bash
docker compose up -d --wait
docker compose exec -T tools mongosh "$SOURCE_URI" --quiet --file scripts/seed.js
```
Two servers start: `mongo-source` (port 27031) and `mongo-target` (port 27032), and a `tools` container where `SOURCE_URI` and `TARGET_URI` are already set. Run every command with `docker compose exec tools bash`.
## Scripts
| Script | What it does |
| --- | --- |
| `scripts/backup.sh <database>` | One compressed archive, written under a temporary name, then renamed. Adds a SHA-256 file and removes old backups (`KEEP`, default 7) |
| `scripts/restore.sh <archive> [target database] [--drop]` | Checks the checksum, then restores, optionally into another database name |
| `scripts/compare.sh <uri A> <db A> <uri B> <db B>` | Compares collections, document counts and index counts |
| `scripts/seed.js` | Fills the lab database `prolang` |
## Try a backup and a restore
```bash
docker compose exec tools bash
MONGO_URI="$SOURCE_URI" bash scripts/backup.sh prolang
MONGO_URI="$TARGET_URI" bash scripts/restore.sh /backups/prolang-<timestamp>.archive.gz prolang_copy
bash scripts/compare.sh "$SOURCE_URI" prolang "$TARGET_URI" prolang_copy
```
## Clean up
```bash
docker compose down -v
```
