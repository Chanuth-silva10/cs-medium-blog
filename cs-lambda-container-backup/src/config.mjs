import { GetSecretValueCommand, SecretsManagerClient } from '@aws-sdk/client-secrets-manager';

/**
 * Where the database is and how to log in.
 *
 * In AWS the credentials live in Secrets Manager (DB_SECRET_ARN), as a JSON text:
 *   {"host":"...","port":5432,"dbname":"...","username":"...","password":"..."}
 * For a local test, DB_HOST, DB_PORT, DB_NAME, DB_USER and DB_PASSWORD can be given directly.
 */
export async function loadDatabaseConfig(env = process.env, secretsClient = new SecretsManagerClient({})) {
  let source = {};
  if (env.DB_SECRET_ARN) {
    const answer = await secretsClient.send(new GetSecretValueCommand({ SecretId: env.DB_SECRET_ARN }));
    source = JSON.parse(answer.SecretString ?? '{}');
  } else {
    source = {
      host: env.DB_HOST,
      port: env.DB_PORT,
      dbname: env.DB_NAME,
      username: env.DB_USER,
      password: env.DB_PASSWORD,
    };
  }

  const config = {
    host: source.host,
    port: String(source.port ?? 5432),
    dbname: source.dbname,
    username: source.username,
    password: source.password,
  };
  const missing = ['host', 'dbname', 'username', 'password'].filter((key) => !config[key]);
  if (missing.length > 0) {
    throw new Error(`The database settings are incomplete, missing: ${missing.join(', ')}`);
  }
  return config;
}

/** The S3 key of a backup: backups/<database>/<year>/<month>/<database>-<date>-<time>.dump */
export function backupKey(dbname, prefix = 'backups/', now = new Date()) {
  const pad = (number) => String(number).padStart(2, '0');
  const year = now.getUTCFullYear();
  const month = pad(now.getUTCMonth() + 1);
  const stamp = `${year}${month}${pad(now.getUTCDate())}-${pad(now.getUTCHours())}${pad(now.getUTCMinutes())}${pad(now.getUTCSeconds())}`;
  const cleanPrefix = prefix && !prefix.endsWith('/') ? `${prefix}/` : prefix;
  return `${cleanPrefix}${dbname}/${year}/${month}/${dbname}-${stamp}.dump`;
}
