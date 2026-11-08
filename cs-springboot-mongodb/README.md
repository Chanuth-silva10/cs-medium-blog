# CS Spring Boot MongoDB
MongoDB with Spring Boot 4 and Java 21: running clubs and their runners. Documents, nested objects and references, automatic timestamps and unique indexes (Chapter 24), then query methods, sorting and paging (Chapter 25). Part of the DevCraft series by CS Solution.
## Endpoints
| Method | Path | What it does |
| --- | --- | --- |
| POST | `/clubs` | Create a club (name and code are unique) |
| PUT | `/clubs/{id}` | Update a club |
| DELETE | `/clubs/{id}` | Delete a club, its runners stay |
| POST | `/clubs/{id}/members` | Add runners by id, body `["id1","id2"]` |
| GET | `/clubs` | All clubs by name descending, or `?city=` / `?search=` |
| GET | `/clubs/{id}` | One club with its runners |
| GET | `/clubs/of-runner/{runnerId}` | The clubs a runner belongs to |
| POST | `/runners` | Create a runner |
| POST | `/runners/bulk` | Create many runners |
| PUT | `/runners/{id}` | Update a runner |
| DELETE | `/runners/{id}` | Delete a runner and remove it from every club |
| GET | `/runners` | All runners by name, or `?search=` / `?category=&active=` |
| GET | `/runners/{id}` | One runner |
| GET | `/runners/by-categories?categories=MARATHON,TRAIL` | Runners in any of the categories |
| GET | `/runners/born-between?from=&to=` | Runners born between two dates (both ends excluded) |
| GET | `/runners/youngest` | The youngest runner |
| GET | `/runners/oldest` | The five oldest runners |
| GET | `/runners/page?page=0&size=10&sort=name,asc` | A page of runners, pages start at 0 |
## Run
```bash
docker run -d --name devcraft-clubs-mongo -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=devcraft -e MONGO_INITDB_ROOT_PASSWORD=devcraftPwd mongo:7.0
export SEED_DEMO_DATA=true
mvn spring-boot:run
```
The API starts on `http://localhost:7510`. With `SEED_DEMO_DATA=true` it loads 6 clubs with 8 runners each, only if no club exists yet.
## Settings
| Environment variable | Default | Meaning |
| --- | --- | --- |
| `MONGO_HOST` / `MONGO_PORT` | `localhost` / `27017` | Where MongoDB runs |
| `MONGO_USER` / `MONGO_PASSWORD` | `devcraft` / `devcraftPwd` | Credentials (authentication database `admin`) |
| `MONGO_DATABASE` | `devcraft_clubs` | Database name |
| `SEED_DEMO_DATA` | `false` | Load demo data at startup |
## Test
The tests need MongoDB running as above. They use their own database, `devcraft_clubs_test`, and empty it before every test.
```bash
mvn test
```
