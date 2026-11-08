import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  // Writes a self-contained server in .next/standalone, with only the files the app really uses.
  output: 'standalone',
};

export default nextConfig;
