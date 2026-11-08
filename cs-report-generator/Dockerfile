# Puppeteer needs a real Chromium, so we use the Debian-based Node image and install it.
FROM node:22-bookworm-slim

ENV PUPPETEER_SKIP_DOWNLOAD=true
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium

RUN apt-get update \
  && apt-get install -y --no-install-recommends chromium ca-certificates fonts-liberation \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
# Compile TypeScript, then drop the dev dependencies the running service does not need.
RUN npm run build && npm prune --omit=dev

ENV NODE_ENV=production
USER node

EXPOSE 4600

ENTRYPOINT ["node", "dist/index.js"]
