import { GraphQLError } from 'graphql';
import { members } from './datasource.js';
import type { Resolvers } from './generated/types.js';
import { dateTimeScalar } from './scalars/datetime.js';

export const resolvers: Resolvers = {
  DateTime: dateTimeScalar,
  Query: {
    // joinedAfter arrives here already as a Date: the scalar did the parsing.
    members: (_parent, { joinedAfter }) =>
      members
        .filter((member) => !joinedAfter || member.joinedAt.getTime() > joinedAfter.getTime())
        .sort((a, b) => a.joinedAt.getTime() - b.joinedAt.getTime()),
  },
  Mutation: {
    registerMember: (_parent, { input }) => {
      if (members.some((member) => member.email === input.email)) {
        throw new GraphQLError('A member with this email already exists', { extensions: { code: 'CONFLICT' } });
      }
      // input.joinedAt is a real Date, so Date methods work and the compiler knows it.
      const member = { id: String(members.length + 1), name: input.name, email: input.email, joinedAt: input.joinedAt };
      members.push(member);
      return member;
    },
  },
};
