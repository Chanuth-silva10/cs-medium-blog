// Used by Docker and by monitoring tools to know that the server is alive.
export const dynamic = 'force-dynamic';

export function GET() {
  return Response.json({ status: 'UP' });
}
