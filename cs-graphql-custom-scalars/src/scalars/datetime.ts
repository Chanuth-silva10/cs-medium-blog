import { GraphQLError, GraphQLScalarType, Kind, print } from 'graphql';

// 2026-11-03T09:30:00Z   2026-11-03T09:30:00.123+05:30   (a time zone is mandatory)
const ISO_DATE_TIME = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d{1,3}))?(Z|[+-]\d{2}:\d{2})$/;

const invalid = (reason: string): GraphQLError =>
  new GraphQLError(`DateTime ${reason}`, { extensions: { code: 'BAD_USER_INPUT' } });

/**
 * Turns an ISO 8601 text into a Date, or throws. A regular expression alone is not enough: it accepts
 * "2026-13-45T25:61:61Z", which looks right and is not a date. So after matching the shape, we rebuild
 * the date and check that every part survived (a month 13 or a February 30 would roll over).
 */
export function parseDateTime(value: unknown): Date {
  if (typeof value !== 'string') {
    throw invalid(`must be a string, received ${typeof value}`);
  }
  const match = ISO_DATE_TIME.exec(value);
  if (!match) {
    throw invalid('must look like 2026-11-03T09:30:00Z or 2026-11-03T09:30:00+05:30');
  }
  const [year, month, day, hour, minute, second] = match.slice(1, 7).map(Number) as [
    number, number, number, number, number, number,
  ];
  const millis = match[7] ? Number(match[7].padEnd(3, '0')) : 0;
  const utc = new Date(Date.UTC(year, month - 1, day, hour, minute, second, millis));
  const rolledOver =
    utc.getUTCFullYear() !== year ||
    utc.getUTCMonth() !== month - 1 ||
    utc.getUTCDate() !== day ||
    utc.getUTCHours() !== hour ||
    utc.getUTCMinutes() !== minute ||
    utc.getUTCSeconds() !== second;
  if (rolledOver) {
    throw invalid(`is not a real date or time: ${value}`);
  }
  const zone = match[8] as string;
  if (zone === 'Z') {
    return utc;
  }
  const offsetHours = Number(zone.slice(1, 3));
  const offsetMinutes = Number(zone.slice(4, 6));
  if (offsetHours > 23 || offsetMinutes > 59) {
    throw invalid(`has an impossible time zone offset: ${zone}`);
  }
  const offsetMillis = (offsetHours * 60 + offsetMinutes) * 60_000 * (zone.startsWith('-') ? -1 : 1);
  return new Date(utc.getTime() - offsetMillis);
}

export const dateTimeScalar = new GraphQLScalarType<Date, string>({
  name: 'DateTime',
  description: 'A moment in time, as an ISO 8601 text with a time zone',
  specifiedByURL: 'https://datatracker.ietf.org/doc/html/rfc3339',

  // Response: the Date in our code becomes a text the client can read.
  serialize(value) {
    if (!(value instanceof Date) || Number.isNaN(value.getTime())) {
      throw new GraphQLError('DateTime cannot represent a value that is not a valid Date');
    }
    return value.toISOString();
  },

  // Input given as a variable: {"joinedAt": "..."} becomes a Date in our code.
  parseValue: parseDateTime,

  // Input written directly inside the query text: joinedAfter: "...". Only a string is accepted,
  // and the error names the exact piece of the query that is wrong.
  parseLiteral(ast) {
    if (ast.kind !== Kind.STRING) {
      throw new GraphQLError(`DateTime cannot represent a non string value: ${print(ast)}`, {
        nodes: ast,
        extensions: { code: 'BAD_USER_INPUT' },
      });
    }
    return parseDateTime(ast.value);
  },
});
