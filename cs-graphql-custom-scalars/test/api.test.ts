import type { ApolloServer } from '@apollo/server';
import { beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { members } from '../src/datasource.js';
import { createServer } from '../src/server.js';

const initialMembers = members.map((member) => ({ ...member }));

let server: ApolloServer;

beforeAll(async () => {
  server = createServer();
  await server.start();
});

beforeEach(() => {
  members.splice(0, members.length, ...initialMembers.map((member) => ({ ...member })));
});

async function run(query: string, variables?: Record<string, unknown>) {
  const response = await server.executeOperation({ query, variables });
  if (response.body.kind !== 'single') {
    throw new Error('Expected a single result');
  }
  return response.body.singleResult;
}

const REGISTER = `
  mutation Register($input: RegisterMemberInput!) {
    registerMember(input: $input) { id name joinedAt }
  }`;

describe('GraphQL API with the DateTime scalar', () => {
  it('returns every date as UTC text', async () => {
    const result = await run('{ members { name joinedAt } }');
    expect(result.errors).toBeUndefined();
    expect(result.data).toEqual({
      members: [
        { name: 'Tharindu Perera', joinedAt: '2025-09-18T11:34:25.932Z' },
        { name: 'Imani Fernando', joinedAt: '2025-11-02T04:15:00.000Z' },
      ],
    });
  });

  it('parses an input variable into a Date and returns it in UTC', async () => {
    const result = await run(REGISTER, {
      input: { name: 'Sara Silva', email: 'sara@example.com', joinedAt: '2026-11-03T09:30:00+05:30' },
    });
    expect(result.errors).toBeUndefined();
    expect(result.data).toEqual({ registerMember: { id: '3', name: 'Sara Silva', joinedAt: '2026-11-03T04:00:00.000Z' } });
    expect(members[2]?.joinedAt).toBeInstanceOf(Date);
    expect(members[2]?.joinedAt.getTime()).toBe(Date.UTC(2026, 10, 3, 4, 0, 0));
  });

  it('uses a Date argument written inside the query to filter', async () => {
    const result = await run('{ members(joinedAfter: "2025-10-01T00:00:00Z") { name } }');
    expect(result.data).toEqual({ members: [{ name: 'Imani Fernando' }] });
  });

  it('uses a Date argument sent as a variable to filter', async () => {
    const result = await run('query ($after: DateTime) { members(joinedAfter: $after) { name } }', {
      after: '2025-10-01T00:00:00Z',
    });
    expect(result.data).toEqual({ members: [{ name: 'Imani Fernando' }] });
  });

  it('refuses an impossible date and says why', async () => {
    const result = await run(REGISTER, {
      input: { name: 'Bad Date', email: 'bad@example.com', joinedAt: '2026-02-30T10:00:00Z' },
    });
    expect(result.data).toBeUndefined();
    expect(result.errors?.[0]?.message).toContain('is not a real date or time: 2026-02-30T10:00:00Z');
    expect(members).toHaveLength(2);
  });

  it('refuses a number written where a date is expected, naming the number', async () => {
    const result = await run('{ members(joinedAfter: 5) { name } }');
    expect(result.errors?.[0]?.message).toContain('DateTime cannot represent a non string value: 5');
  });

  it('refuses a date without a time zone', async () => {
    const result = await run(REGISTER, {
      input: { name: 'No Zone', email: 'zone@example.com', joinedAt: '2026-11-03T09:30:00' },
    });
    expect(result.errors?.[0]?.message).toContain('must look like');
  });

  it('refuses a second member with the same email', async () => {
    const result = await run(REGISTER, {
      input: { name: 'Twin', email: 'imani@example.com', joinedAt: '2026-11-03T04:00:00Z' },
    });
    expect(result.errors?.[0]?.extensions?.code).toBe('CONFLICT');
  });

  it('does not expose a password field any more', async () => {
    const result = await run('{ members { password } }');
    expect(result.errors?.[0]?.message).toContain('Cannot query field "password"');
  });
});
