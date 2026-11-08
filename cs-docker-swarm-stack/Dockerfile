FROM node:22-alpine
ARG APP_VERSION=dev
ARG BROKEN=false
ENV NODE_ENV=production APP_VERSION=$APP_VERSION BROKEN=$BROKEN
WORKDIR /app
COPY --chown=node:node app/server.mjs ./server.mjs
USER node
EXPOSE 3000
# Swarm uses this to decide whether a new replica is ready, and to replace one that stops answering.
HEALTHCHECK --interval=5s --timeout=2s --start-period=3s --retries=2 \
  CMD wget -qO- http://127.0.0.1:3000/health > /dev/null || exit 1
CMD ["node", "server.mjs"]
