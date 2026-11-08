import { GraphQLScalarType, Kind } from 'graphql';
import { describe, expect, it } from 'vitest';
import { dateTimeScalar, parseDateTime } from '../src/scalars/datetime.js';

describe('parseDateTime', () => {
  it('reads a UTC time', () => {
    expect(parseDateTime('2026-11-03T04:00:00Z').toISOString()).toBe('2026-11-03T04:00:00.000Z');
  });

  it('converts an offset to the same instant in UTC', () => {
    expect(parseDateTime('2026-11-03T09:30:00+05:30').toISOString()).toBe('2026-11-03T04:00:00.000Z');
    expect(parseDateTime('2026-11-02T23:00:00-04:00').toISOString()).toBe('2026-11-03T03:00:00.000Z');
  });

  it('keeps milliseconds, even when written with one or two digits', () => {
    expect(parseDateTime('2026-11-03T04:00:00.123Z').getUTCMilliseconds()).toBe(123);
    expect(parseDateTime('2026-11-03T04:00:00.5Z').getUTCMilliseconds()).toBe(500);
  });

  it('accepts the 29th of February only in a leap year', () => {
    expect(parseDateTime('2024-02-29T10:00:00Z').getUTCDate()).toBe(29);
    expect(() => parseDateTime('2025-02-29T10:00:00Z')).toThrow('is not a real date');
  });

  it.each([
    ['plain text', 'yesterday'],
    ['a date without a time', '2026-11-03'],
    ['a time without a zone', '2026-11-03T04:00:00'],
    ['month 13 and hour 25', '2026-13-45T25:61:61Z'],
    ['a 30th of February', '2026-02-30T10:00:00Z'],
    ['hour 24', '2026-11-03T24:00:00Z'],
    ['minute 60', '2026-11-03T10:60:00Z'],
    ['an impossible offset', '2026-11-03T10:00:00+25:00'],
    ['trailing text', '2026-11-03T04:00:00Z and more'],
  ])('refuses %s', (_label, value) => {
    expect(() => parseDateTime(value)).toThrow(/DateTime/);
  });

  it('refuses a value that is not a string', () => {
    expect(() => parseDateTime(1794000000000)).toThrow('must be a string, received number');
  });

  it('shows why a regular expression alone is not enough', () => {
    const naive = /(\d{4})-(\d{2})-(\d{2})T((\d{2}):(\d{2}):(\d{2}))\.(\d{3})Z/;
    expect(naive.test('2026-13-45T25:61:61.000Z')).toBe(true);
    expect(Number.isNaN(new Date('2026-13-45T25:61:61.000Z').getTime())).toBe(true);
  });
});

describe('dateTimeScalar', () => {
  it('serializes a Date as UTC text', () => {
    expect(dateTimeScalar.serialize(new Date('2026-11-03T04:00:00Z'))).toBe('2026-11-03T04:00:00.000Z');
  });

  it('refuses to serialize an invalid Date or something that is not a Date', () => {
    expect(() => dateTimeScalar.serialize(new Date('nope'))).toThrow('not a valid Date');
    expect(() => dateTimeScalar.serialize('2026-11-03T04:00:00Z')).toThrow('not a valid Date');
  });

  it('parses a string literal written in the query', () => {
    const date = dateTimeScalar.parseLiteral({ kind: Kind.STRING, value: '2026-11-03T04:00:00Z' });
    expect(date).toEqual(new Date('2026-11-03T04:00:00Z'));
  });

  it('names the offending piece of the query when the literal is not a string', () => {
    expect(() => dateTimeScalar.parseLiteral({ kind: Kind.INT, value: '5' })).toThrow('non string value: 5');
  });

  it('would fall back on parseValue if parseLiteral was not written', () => {
    const minimal = new GraphQLScalarType({ name: 'Minimal', parseValue: (value) => parseDateTime(value) });
    expect(minimal.parseLiteral({ kind: Kind.STRING, value: '2026-11-03T04:00:00Z' }, undefined)).toEqual(
      new Date('2026-11-03T04:00:00Z'),
    );
    // It works, but the message for a number is the generic one of parseValue.
    expect(() => minimal.parseLiteral({ kind: Kind.INT, value: '5' }, undefined)).toThrow(
      'must be a string, received number',
    );
  });
});
