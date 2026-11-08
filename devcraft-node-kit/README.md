# DevCraft Node Kit

A clean, modern Node.js starter with **TypeScript**, **ESLint 9 (flat config)**, and **Prettier** — the base project for the whole DevCraft series by Chanuth Silva.

## Requirements

- Node.js 22+
- npm 10+

## Getting started

```bash
# install dependencies
npm install

# copy the env file
cp .env.example .env

# run in watch mode (tsx, no build step)
npm run dev
```

## Scripts

| Script             | What it does                                         |
| ------------------ | ---------------------------------------------------- |
| `npm run dev`      | Runs `src/index.ts` with tsx + watch + `--env-file`  |
| `npm run build`    | Compiles TypeScript into `dist/`                     |
| `npm start`        | Runs the compiled app from `dist/`                   |
| `npm run lint`     | Checks the code with ESLint                          |
| `npm run lint:fix` | Fixes auto-fixable ESLint problems                   |
| `npm run format`   | Formats the code with Prettier                       |

## License

MIT © Chanuth Silva
