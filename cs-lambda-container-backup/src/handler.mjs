import { spawn } from 'node:child_process';
import { DeleteObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { Upload } from '@aws-sdk/lib-storage';
import { backupKey, loadDatabaseConfig } from './config.mjs';

// forcePathStyle and the endpoint are only needed to talk to a local S3 clone such as MinIO.
const s3 = new S3Client({ forcePathStyle: process.env.S3_FORCE_PATH_STYLE === 'true' });

/**
 * Backs up the database to S3 without writing a file on the disk:
 *   pg_dump  -->  (stdout pipe)  -->  S3 multipart upload
 * The custom format of pg_dump is already compressed, so no gzip step is needed, and it can be
 * restored (even a single table) with pg_restore.
 */
export const handler = async () => {
  const bucket = process.env.BACKUP_BUCKET;
  if (!bucket) {
    throw new Error('BACKUP_BUCKET is not set');
  }
  const database = await loadDatabaseConfig();
  const key = backupKey(database.dbname, process.env.BACKUP_PREFIX ?? 'backups/');

  const dump = spawn(
    'pg_dump',
    ['--format=custom', '--no-password', '--host', database.host, '--port', database.port, '--username', database.username, '--dbname', database.dbname],
    // The password goes through the environment of the child process only: it never appears in the
    // command line, which any process of the machine can read.
    { env: { ...process.env, PGPASSWORD: database.password }, stdio: ['ignore', 'pipe', 'pipe'] },
  );

  let errorOutput = '';
  dump.stderr.on('data', (chunk) => {
    errorOutput += chunk.toString();
  });
  const finished = new Promise((resolve, reject) => {
    dump.on('error', reject);
    dump.on('close', (code) => resolve(code));
  });

  const upload = new Upload({
    client: s3,
    params: { Bucket: bucket, Key: key, Body: dump.stdout, ServerSideEncryption: 'AES256', ContentType: 'application/octet-stream' },
  });
  let bytes = 0;
  upload.on('httpUploadProgress', (progress) => {
    bytes = progress.loaded ?? bytes;
  });
  try {
    const [code] = await Promise.all([finished, upload.done()]);
    if (code !== 0) {
      throw new Error(`pg_dump exited with code ${code}: ${errorOutput.trim()}`);
    }
  } catch (error) {
    // A stream that ends early is a complete upload for S3, so a failed pg_dump can leave a short,
    // useless object behind. Remove it, so nobody ever restores from it.
    dump.kill();
    await upload.abort().catch(() => {});
    await s3.send(new DeleteObjectCommand({ Bucket: bucket, Key: key })).catch(() => {});
    // Throwing marks the invocation as failed, so the Errors metric and its alarm see it.
    throw new Error(`Backup of ${database.dbname} failed: ${error.message}`);
  }
  console.log(JSON.stringify({ message: 'Backup completed', bucket, key, bytes }));
  return { status: 'success', bucket, key, bytes };
};
