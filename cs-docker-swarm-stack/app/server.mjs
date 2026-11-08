// A tiny web service for trying Docker Swarm. Every answer says which container replied and which version it is.
import { createServer } from 'node:http';
import { hostname } from 'node:os';

const port = Number(process.env.PORT ?? 3000);
const version = process.env.APP_VERSION ?? 'dev';
// Used to rehearse a bad release: the application starts, but reports itself as unhealthy.
const broken = process.env.BROKEN === 'true';

const server = createServer((request, response) => {
  if (request.url === '/health') {
    response.writeHead(broken ? 503 : 200, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ status: broken ? 'DOWN' : 'UP' }));
    return;
  }
  response.writeHead(200, { 'Content-Type': 'application/json' });
  response.end(JSON.stringify({ app: 'devcraft-whoami', version, container: hostname() }));
});

server.listen(port, () => console.log(`devcraft-whoami ${version} listening on ${port}`));

// docker stop sends SIGTERM: finish the open requests, then leave.
process.on('SIGTERM', () => server.close(() => process.exit(0)));
