# CS Lambda Container Backup
A Lambda function packaged as a container image that backs up a PostgreSQL database to Amazon S3 every week. `pg_dump` streams straight into S3, with no temporary file. Part of the DevCraft series by CS Solution (Chapter 38).
## How it works
`pg_dump --format=custom` writes to a pipe, and the AWS SDK uploads that pipe to S3 as a multipart upload. If `pg_dump` fails, the half-written object is deleted and the invocation fails, so the CloudWatch alarm fires.
## Settings (environment variables of the function)
| Variable | Meaning |
| --- | --- |
| `BACKUP_BUCKET` | The bucket that receives the backups |
| `BACKUP_PREFIX` | Folder in the bucket, default `backups/` |
| `DB_SECRET_ARN` | A Secrets Manager secret with `host`, `port`, `dbname`, `username`, `password` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | The same, for local tests, used when there is no secret |
## Test everything on your computer
```bash
npm install
npm test
bash scripts/local-test.sh
```
The script starts PostgreSQL and a local S3, runs the Lambda image with its built-in emulator, does a backup, restores it into a second database and compares the data, then checks a failure. It needs Docker.
## Deploy
1. Create an ECR repository and push the image (see the Amazon ECR chapter).
2. Create the secret with the database settings.
3. Deploy the template:
```bash
sam deploy --template-file template.yaml --stack-name devcraft-db-backup --capabilities CAPABILITY_IAM \
  --parameter-overrides ImageUri=<ecr image> DatabaseSecretArn=<secret arn> BackupBucketName=<unique bucket name>
```
