# CS Spring Boot Starter

A Spring Boot 4 + Java 21 REST API starter: layered structure, environment profiles, JPA with MySQL, Flyway migrations, validation, Actuator, Swagger UI and Lombok. The base for DevCraft's Spring services by CS Solution. It grows across several chapters (REST API, Flyway migrations, Swagger docs).

## Requirements

- JDK 21+
- Maven 3.9+
- MySQL 8 (for running; compiling and testing need only the JDK and Maven)

## Run

```bash
cp src/main/resources/application-example.properties src/main/resources/application-dev.properties
# edit the database URL, user and password, then:
mvn spring-boot:run
```

The API starts on `http://localhost:7450`.

- `GET /actuator/health` - health check
- `/projects` - create, list, read, update (PATCH) and delete projects
- `/docs` - Swagger UI, `/v3/api-docs` - the OpenAPI document

Logs go to the console and to `logs/app.log` and `logs/error.log`.

## Test

```bash
mvn test
```

The tests boot the whole app on an in-memory H2 database in MySQL mode, so no MySQL server is needed.

## Structure

- `models` - entities and DTOs
- `repositories` - Spring Data repositories
- `services` - business logic
- `controllers` - REST endpoints and the shared error handler
- `configs` - library configuration (OpenAPI)

## License

MIT (c) CS Solution
