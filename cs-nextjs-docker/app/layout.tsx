import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'DevCraft Notes',
  description: 'A small Next.js site packaged with Docker',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
