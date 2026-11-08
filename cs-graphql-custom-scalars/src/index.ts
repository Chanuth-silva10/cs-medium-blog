import { startStandaloneServer } from '@apollo/server/standalone';
import { createServer } from './server.js';

const { url } = await startStandaloneServer(createServer(), { listen: { port: 7580 } });
console.log(`GraphQL API ready at ${url}`);
