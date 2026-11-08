# CS Spring Boot Auth API

JWT authentication and role-based access control for a Spring Boot 4 + Java 21 REST API, with Spring Security 7, JJWT, MySQL and Flyway. Part of the DevCraft series by CS Solution (Chapters 18 and 19).

## Endpoints

| Method | Path | Who |
| --- | --- | --- |
| POST | `/auth/register` | Anyone (creates a MEMBER) |
| POST | `/auth/login` | Anyone, returns a JWT |
| GET | `/users/me` | Any signed-in user |
| GET | `/users` | ADMIN, SUPER_ADMIN |
| POST | `/admins` | SUPER_ADMIN |

## Run

```bash
docker run -d --name devcraft-auth-mysql -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=rootPwd -e MYSQL_DATABASE=devcraft_auth \
  -e MYSQL_USER=devcraft -e MYSQL_PASSWORD=devcraftPwd mysql:8.4

export DB_PASSWORD=devcraftPwd
export JWT_SECRET_KEY=$(openssl rand -base64 32)
export SUPER_ADMIN_EMAIL=super.admin@devcraft.io
export SUPER_ADMIN_PASSWORD=change-me-to-a-long-password
mvn spring-boot:run
```

The API starts on `http://localhost:7470`. All settings are listed in `.env.example`.

## Test

```bash
mvn test
```

The tests use an in-memory H2 database, so no MySQL is needed.

## License

MIT (c) CS Solution
