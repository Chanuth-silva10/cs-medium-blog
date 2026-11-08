# CS Mongoose Starter

A Node.js + TypeScript starter wired to **MongoDB** with **Mongoose 8** — the data layer for the DevCraft series by CS Solution. It models a small **Tech Articles** platform: authors write articles, articles carry nested comments.

## Requirements

- Node.js 22+
- A running MongoDB 7 instance (local or Docker)

## Quick start

```bash
npm install
cp .env.example .env
# start a MongoDB container if you need one:
# docker run -d -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=devcraft -e MONGO_INITDB_ROOT_PASSWORD=devcraftPwd --name devcraft-mongo mongo:7.0
npm run dev
```

## Scripts

| Script             | What it does                                     |
| ------------------ | ------------------------------------------------ |
| `npm run dev`      | Runs `src/index.ts` with tsx + watch + env-file  |
| `npm run build`    | Compiles TypeScript into `dist/`                 |
| `npm start`        | Runs the compiled app                            |
| `npm run lint`     | Lints the code                                   |
| `npm run format`   | Formats the code with Prettier                   |

## What's inside

- `src/db-connection.ts` — connect/disconnect helpers (URI from env)
- `src/models/author.model.ts` — Author schema
- `src/models/article.model.ts` — Article schema with nested comments + topic enum
- `src/queries/*` — insert, retrieve, update, delete examples

## License

MIT © CS Solution
