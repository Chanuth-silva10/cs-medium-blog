// Rendered on every request, so the server reads its environment each time.
export const dynamic = 'force-dynamic';

export default function HomePage() {
  // NEXT_PUBLIC_* values are replaced by their text at BUILD time, wherever they appear in the code.
  const siteName = process.env.NEXT_PUBLIC_SITE_NAME;
  // Any other variable is read from the running server, at REQUEST time.
  const environment = process.env.APP_ENV_LABEL ?? 'not set';

  return (
    <main>
      <h1>{siteName}</h1>
      <p>Packaged with Docker, served by Next.js.</p>
      <dl>
        <dt>Site name (baked at build time)</dt>
        <dd data-testid="site-name">{siteName}</dd>
        <dt>Environment label (read at run time)</dt>
        <dd data-testid="environment">{environment}</dd>
        <dt>Node.js</dt>
        <dd>{process.version}</dd>
        <dt>Running as user id</dt>
        <dd>{process.getuid?.() ?? 'unknown'}</dd>
      </dl>
    </main>
  );
}
