# CS Report Generator

A small report generator service: Node.js + Express generates project reports as PDF with Puppeteer, and stores report metadata in MongoDB. Run the whole thing with Docker Compose. Part of the DevCraft series by CS Solution.

## Run with Docker Compose

```bash
docker compose up --build
```

The API comes up on `http://localhost:4600` with a MongoDB container alongside it. The app waits for MongoDB's health check before it starts.

## Endpoints

- `POST /reports` - create a report and download it as a PDF
- `GET /reports` - list saved report metadata

```bash
curl -X POST http://localhost:4600/reports \
  -H "Content-Type: application/json" \
  -d '{"projectName":"DevCraft","author":"CS Solution","summary":"Quarterly status."}' \
  --output report.pdf
```

All three fields are required strings. Limits: `projectName` and `author` up to 100 characters, `summary` up to 5000.

## Run without Docker

The `.env` file points at the `mongodb` host, which only exists inside the Compose network. To run on your machine, change `MONGODB_URI` to use `localhost`, set `PUPPETEER_EXECUTABLE_PATH` to a local Chrome or Chromium, and run `npm run dev`.

## License

MIT (c) CS Solution
