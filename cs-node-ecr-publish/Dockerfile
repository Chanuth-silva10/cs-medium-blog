# syntax=docker/dockerfile:1

# ---- build stage ----
FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
# --ignore-scripts skips the "prepare" hook, which installs Git hooks and needs a git repo that is not in the image.
RUN npm ci --ignore-scripts

COPY . .
# Bundle the app into one file. swagger-ui-dist stays external: it serves its CSS/JS from disk, which a bundle cannot do.
RUN npx esbuild ./src/index.ts --bundle --platform=node --format=esm --outfile=dist/index.js --external:swagger-ui-dist \
  --banner:js="import { createRequire as __cr } from 'module'; import { fileURLToPath as __f } from 'url'; import { dirname as __d } from 'path'; const require = __cr(import.meta.url); const __filename = __f(import.meta.url); const __dirname = __d(__filename);"

# ---- runtime stage ----
FROM node:22-alpine AS app

ENV NODE_ENV=production

WORKDIR /app

COPY --chown=node:node --from=builder /app/dist/index.js /app/index.js
COPY --chown=node:node --from=builder /app/node_modules/swagger-ui-dist /app/node_modules/swagger-ui-dist

USER node

EXPOSE 4500
# The public route / answers without the database, so it tells us the process is alive.
HEALTHCHECK --interval=15s --timeout=3s --start-period=20s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1:4500/').then((r) => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"

ENTRYPOINT ["node", "index.js"]

