import { readFileSync } from 'node:fs';
import { ApolloServer } from '@apollo/server';
import { resolvers } from './resolvers.js';

const typeDefs = readFileSync(new URL('./schema.graphql', import.meta.url), 'utf8');

export const createServer = (): ApolloServer => new ApolloServer({ typeDefs, resolvers });
