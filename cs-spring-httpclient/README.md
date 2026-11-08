# CS Spring HTTP Client

A Spring Boot 4 + Java 21 service that calls another HTTP API: the DevCraft Task API from the Node.js chapters. The same client is written twice, once with RestTemplate and once with RestClient, so you can compare them. Part of the DevCraft series by CS Solution.

## What it shows

- Calling GET, POST, PATCH and DELETE endpoints with headers and JSON bodies
- Logging in once and reusing the bearer token, with one automatic retry when it expires
- Timeouts on every call (a plain `new RestTemplate()` has none)
- Turning upstream errors into honest answers: 404/403/422 passed on, 502 for upstream failures, 503 when it is down, 504 when it is too slow
- One set of tests that runs against both clients, using a small fake HTTP server

## Requirements

- JDK 21+
- Maven 3.9+
- The DevCraft Task API running (see `../cs-devcraft-task-api`) for a live run. The tests do not need it.

## Run

Create a service account on the Task API (`POST /auth/register`), then:

```bash
export TASK_API_URL=http://localhost:4500
export TASK_API_EMAIL=your-service-account@example.com
export TASK_API_PASSWORD=your-password
mvn spring-boot:run
```

The service starts on `http://localhost:7460`. Switch implementations with `--devcraft.task-api.client=rest-client`.

| Our endpoint | Calls on the Task API |
| --- | --- |
| `GET /tasks` | `GET /tasks/me` |
| `GET /tasks/{id}` | `GET /tasks/{id}` |
| `POST /tasks` | `POST /tasks` |
| `PATCH /tasks/{id}` | `PATCH /tasks/{id}` |
| `DELETE /tasks/{id}` | `DELETE /tasks/{id}` |
| `GET /categories` | `GET /categories` |
| `POST /categories` | `POST /categories` |

## Test

```bash
mvn test
```

## License

MIT (c) CS Solution
