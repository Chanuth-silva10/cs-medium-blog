# syntax=docker/dockerfile:1

# Stage 1: install the dependencies. This layer is rebuilt only when package.json or the lockfile change.
FROM node:22-alpine AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# Stage 2: build the application.
FROM node:22-alpine AS builder
WORKDIR /app
# NEXT_PUBLIC_* values are written into the JavaScript at build time, so they must be known HERE.
ARG SITE_NAME="DevCraft Notes"
ENV NEXT_PUBLIC_SITE_NAME=$SITE_NAME
ENV NEXT_TELEMETRY_DISABLED=1
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Stage 3: the image that runs. It receives only the build output, no source and no dev dependencies.
FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
# Listen on every interface of the container. Without it, the server binds to the container's own
# address only, and the health check on 127.0.0.1 inside the container is refused.
ENV HOSTNAME=0.0.0.0
ENV PORT=3000
RUN addgroup --system --gid 1001 nextgroup && adduser --system --uid 1001 --ingroup nextgroup nextuser
# A "public" folder would be copied here if the project had one.
COPY --from=builder --chown=nextuser:nextgroup /app/.next/standalone ./
COPY --from=builder --chown=nextuser:nextgroup /app/.next/static ./.next/static
USER nextuser
EXPOSE 3000
HEALTHCHECK --interval=15s --timeout=3s --start-period=10s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1:3000/api/health').then((r) => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"
CMD ["node", "server.js"]

