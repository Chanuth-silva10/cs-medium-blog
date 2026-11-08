import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: false,
  },
  test: {
    // globals lets Testing Library clean the page between tests by itself.
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
  },
});

