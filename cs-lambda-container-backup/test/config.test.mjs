import { describe, expect, it } from 'vitest';
import { backupKey, loadDatabaseConfig } from '../src/config.mjs';

describe('backupKey', () => {
  const moment = new Date('2026-11-07T02:30:05Z');

  it('puts the database, the year and the month in the path', () => {
    expect(backupKey('shop', 'backups/', moment)).toBe('backups/shop/2026/11/shop-20261107-023005.dump');
  });

  it('adds the slash that is missing at the end of the prefix', () => {
    expect(backupKey('shop', 'weekly', moment)).toBe('weekly/shop/2026/11/shop-20261107-023005.dump');
  });

  it('works without a prefix', () => {
    expect(backupKey('shop', '', moment)).toBe('shop/2026/11/shop-20261107-023005.dump');
  });

  it('uses UTC, so the name does not depend on the time zone of the machine', () => {
    expect(backupKey('shop', 'b/', new Date('2026-12-31T23:59:59Z'))).toBe('b/shop/2026/12/shop-20261231-235959.dump');
  });
});

describe('loadDatabaseConfig', () => {
  it('reads plain environment variables', async () => {
    const config = await loadDatabaseConfig({ DB_HOST: 'db', DB_PORT: '5433', DB_NAME: 'shop', DB_USER: 'u', DB_PASSWORD: 'p' });
    expect(config).toEqual({ host: 'db', port: '5433', dbname: 'shop', username: 'u', password: 'p' });
  });

  it('defaults the port to 5432', async () => {
    const config = await loadDatabaseConfig({ DB_HOST: 'db', DB_NAME: 'shop', DB_USER: 'u', DB_PASSWORD: 'p' });
    expect(config.port).toBe('5432');
  });

  it('reads the settings from Secrets Manager when a secret is given', async () => {
    const secrets = {
      send: async (command) => {
        expect(command.input.SecretId).toBe('arn:aws:secretsmanager:eu-west-2:123456789012:secret:db-AbCdEf');
        return { SecretString: JSON.stringify({ host: 'prod-db', port: 5432, dbname: 'shop', username: 'backup', password: 's3cret' }) };
      },
    };
    const config = await loadDatabaseConfig({ DB_SECRET_ARN: 'arn:aws:secretsmanager:eu-west-2:123456789012:secret:db-AbCdEf' }, secrets);
    expect(config).toEqual({ host: 'prod-db', port: '5432', dbname: 'shop', username: 'backup', password: 's3cret' });
  });

  it('names every missing setting without printing the password', async () => {
    await expect(loadDatabaseConfig({ DB_HOST: 'db', DB_PASSWORD: 'p' })).rejects.toThrow('missing: dbname, username');
  });
});
