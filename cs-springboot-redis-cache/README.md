# CS Spring Boot Redis Cache

A product catalogue API on Spring Boot 4 + Java 21 that caches its slow reads in Redis with the Spring cache abstraction: per-cache TTLs, JSON values, normalized search keys, eviction on every change, protection against cache stampedes and graceful fallback when Redis is down. Part of the DevCraft series by CS Solution (Chapter 20).

## Endpoints

| Method | Path | Cached |
| --- | --- | --- |
| GET | `/categories` | yes, 1 hour |
| GET | `/products` | yes, 10 minutes |
| GET | `/products/search?name=&category=&minPrice=&maxPrice=&available=` | yes, 2 minutes per search |
| POST | `/products` | clears the product caches |
| PATCH | `/products/{id}` | clears the product caches |
| DELETE | `/products/{id}` | clears the product caches |

Every database read waits `app.catalog.simulated-delay` (2 seconds by default) to make the effect of the cache visible.

## Run

```bash
docker run -d --name devcraft-redis -p 6379:6379 redis:8-alpine
docker run -d --name devcraft-catalog-mysql -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=rootPwd -e MYSQL_DATABASE=devcraft_catalog \
  -e MYSQL_USER=devcraft -e MYSQL_PASSWORD=devcraftPwd mysql:8.4

export DB_PASSWORD=devcraftPwd
mvn spring-boot:run
```

The API starts on `http://localhost:7480` and Flyway loads 5 categories and 24 products. Look inside the cache with `docker exec -it devcraft-redis redis-cli --scan --pattern 'devcraft:catalog:*'`.

## Test

```bash
mvn test
```

The tests use H2 and Spring's in-memory cache, so neither MySQL nor Redis is needed.

## License

MIT (c) CS Solution
