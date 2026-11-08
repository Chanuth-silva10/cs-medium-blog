#!/usr/bin/env bash
# Tests the whole backup on your computer: PostgreSQL and S3Mock (a local S3) in containers, the
# Lambda image with its built-in emulator, a backup, a restore into a second database, and a failure.
# Needs Docker. Run it from the project folder:  bash scripts/local-test.sh
set -uo pipefail

NET=devcraft-backup-net
IMAGE=devcraft/db-backup:local
PGPASS=devcraftPwd
S3_USER=local-access-key
S3_PASS=local-secret-key
BUCKET=db-backups

cleanup() {
  docker rm -f bk-lambda bk-postgres bk-s3 > /dev/null 2>&1
  docker network rm "$NET" > /dev/null 2>&1
}
trap cleanup EXIT
cleanup

echo "== Build the image"
docker build -q -t "$IMAGE" . > /dev/null || exit 1

docker network create "$NET" > /dev/null
echo "== Start PostgreSQL 16 and S3Mock"
docker run -d --name bk-postgres --network "$NET" -e POSTGRES_PASSWORD="$PGPASS" -e POSTGRES_DB=shop postgres:16-alpine > /dev/null
docker run -d --name bk-s3 --network "$NET" -e COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS="$BUCKET" adobe/s3mock:latest > /dev/null
for _ in $(seq 1 30); do
  docker exec bk-postgres pg_isready -U postgres -d shop > /dev/null 2>&1 && break
  sleep 1
done
sleep 8

echo "== Put 50000 rows in the database"
docker exec bk-postgres psql -U postgres -d shop -q \
  -c "CREATE TABLE orders (id serial PRIMARY KEY, customer text NOT NULL, total numeric(10,2) NOT NULL, created_at timestamptz DEFAULT now());" \
  -c "INSERT INTO orders (customer, total) SELECT 'customer-' || g, (g % 500) + 0.99 FROM generate_series(1, 50000) g;"
docker exec bk-postgres psql -U postgres -d shop -At -c "SELECT count(*) FROM orders"

S3_ENV=(-e AWS_ENDPOINT_URL_S3=http://bk-s3:9090 -e AWS_ACCESS_KEY_ID="$S3_USER" -e AWS_SECRET_ACCESS_KEY="$S3_PASS" -e AWS_REGION=eu-west-2 -e S3_FORCE_PATH_STYLE=true)

echo "== Start the Lambda container"
docker run -d --name bk-lambda --network "$NET" -p 9000:8080 "${S3_ENV[@]}" \
  -e BACKUP_BUCKET="$BUCKET" -e DB_HOST=bk-postgres -e DB_NAME=shop -e DB_USER=postgres -e DB_PASSWORD="$PGPASS" "$IMAGE" > /dev/null
sleep 3

invoke() {
  curl -s -XPOST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{}'
  echo
}

echo "== Invoke: backup"
START=$(date +%s)
RESULT="$(invoke)"
echo "$RESULT"
echo "took $(( $(date +%s) - START )) s"
KEY="$(echo "$RESULT" | sed -n 's/.*"key":"\([^"]*\)".*/\1/p')"

echo "== Restore the backup into a new database"
docker exec bk-postgres createdb -U postgres restored
docker run --rm --network "$NET" "${S3_ENV[@]}" -e PGPASSWORD="$PGPASS" -e KEY="$KEY" --entrypoint sh "$IMAGE" -c "
node -e \"
import('@aws-sdk/client-s3').then(async ({ S3Client, GetObjectCommand }) => {
  const s3 = new S3Client({ forcePathStyle: true });
  const object = await s3.send(new GetObjectCommand({ Bucket: '$BUCKET', Key: process.env.KEY }));
  object.Body.pipe(process.stdout);
});\" > /tmp/backup.dump
ls -l /tmp/backup.dump | awk '{print \"downloaded bytes: \" \$5}'
pg_restore --no-owner --host bk-postgres --username postgres --dbname restored /tmp/backup.dump && echo 'pg_restore finished'
"
echo -n "rows in the restored database: "
docker exec bk-postgres psql -U postgres -d restored -At -c "SELECT count(*) FROM orders"
A="$(docker exec bk-postgres psql -U postgres -d shop -At -c 'SELECT sum(total) FROM orders')"
B="$(docker exec bk-postgres psql -U postgres -d restored -At -c 'SELECT sum(total) FROM orders')"
if [ "$A" = "$B" ]; then echo "same total in both databases: yes ($A)"; else echo "same total in both databases: NO ($A vs $B)"; fi

echo "== Invoke with a wrong password"
docker rm -f bk-lambda > /dev/null
docker run -d --name bk-lambda --network "$NET" -p 9000:8080 "${S3_ENV[@]}" \
  -e BACKUP_BUCKET="$BUCKET" -e DB_HOST=bk-postgres -e DB_NAME=shop -e DB_USER=postgres -e DB_PASSWORD=wrong-password "$IMAGE" > /dev/null
sleep 3
invoke

echo "== What is in the bucket now"
docker run --rm --network "$NET" "${S3_ENV[@]}" --entrypoint node "$IMAGE" -e "
import('@aws-sdk/client-s3').then(async ({ S3Client, ListObjectsV2Command }) => {
  const s3 = new S3Client({ forcePathStyle: true });
  const list = await s3.send(new ListObjectsV2Command({ Bucket: '$BUCKET' }));
  for (const object of list.Contents ?? []) console.log(object.Key, object.Size);
  console.log('objects:', (list.Contents ?? []).length);
});"