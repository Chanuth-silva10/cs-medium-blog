# DevCraft Task API

A project and task management REST API built with Node.js, Express and MongoDB. Part of the DevCraft series by CS Solution. This project grows across several chapters (auth, roles, docs, Docker, CI/CD).

## Requirements

- Node.js 22+
- A running MongoDB 7 instance

## Quick start

```bash
npm install
cp .env.example .env
npm run dev
```

## Endpoints

Categories:

- `POST /categories`
- `GET /categories`
- `GET /categories/:id`
- `PUT /categories/:id`
- `DELETE /categories/:id`

Tasks:

- `POST /tasks`
- `GET /tasks`
- `GET /tasks/:id`
- `PATCH /tasks/:id`
- `DELETE /tasks/:id`

## License

MIT (c) CS Solution
