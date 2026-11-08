# CS Spring Boot Compose Dev
Spring Boot 4 (Java 21) starts its own MySQL with Docker Compose support. Run the application and the database container is created, started and connected for you. It is the JWT and roles API of Chapters 18 and 19, so the new part is only `compose.yaml`, the `spring-boot-docker-compose` dependency and a few properties. Part of the DevCraft series by CS Solution (Chapter 27).
## Run
You need JDK 21, Maven and Docker with the Compose plugin.
```bash
mvn spring-boot:run
```
The API starts on `http://localhost:7530`. The first start also pulls `mysql:8.4` and takes longer. A development super admin exists: `root@devcraft.io` with `RootPassword123!`. These values are throwaway, in `application-dev.properties`.
## What to look at
| File | Why |
| --- | --- |
| `compose.yaml` | The MySQL service, with a health check and a random host port |
| `application.properties` | No datasource settings: Docker Compose support provides them |
| `application-dev.properties` | `lifecycle-management=start-only`, throwaway secrets |
| `application-prod.properties` | Compose support off, datasource and secrets from the environment |
| `docs/compose.custom-image.yaml` | A custom image with the `service-connection` label |
## Production
```bash
mvn package
SPRING_PROFILES_ACTIVE=prod DB_URL=jdbc:mysql://db-host:3306/devcraft_auth DB_USERNAME=app DB_PASSWORD=... JWT_SECRET_KEY=... java -jar target/cs-springboot-compose-dev-1.0.0.jar
```
## Test
```bash
mvn test
```
The tests use H2 and do not start Docker.
