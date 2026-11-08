# CS Spring Boot Bookshelf

Many-to-many relationships with JPA and Hibernate on Spring Boot 4 + Java 21, MySQL and Flyway. Readers keep a list of favourite books (a plain many-to-many) and review books with a rating and a comment (a many-to-many with extra columns and a composite key). Part of the DevCraft series by CS Solution (Chapters 21 and 22).

## Endpoints

| Method | Path | What it does |
| --- | --- | --- |
| GET | `/books` | All books |
| DELETE | `/books/{bookId}` | Delete a book, with its favourites and reviews |
| GET | `/readers` | Readers with their favourite titles (one query) |
| GET | `/readers/{readerId}/favourites` | A reader's favourite books |
| PUT | `/readers/{readerId}/favourites/{bookId}` | Add a favourite (201 new, 200 already there) |
| DELETE | `/readers/{readerId}/favourites/{bookId}` | Remove a favourite |
| GET | `/books/{bookId}/fans` | Readers who favourited a book |
| PUT | `/readers/{readerId}/reviews/{bookId}` | Create (201) or update (200) a review, body `{"rating":1-5,"comment":"..."}` |
| GET | `/books/{bookId}/reviews` | Average rating, count and reviews of a book |
| GET | `/readers/{readerId}/reviews` | A reader's reviews |
| DELETE | `/readers/{readerId}/reviews/{bookId}` | Delete a review |

## Run

```bash
docker run -d --name devcraft-bookshelf-mysql -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=rootPwd -e MYSQL_DATABASE=devcraft_bookshelf \
  -e MYSQL_USER=devcraft -e MYSQL_PASSWORD=devcraftPwd mysql:8.4

export DB_PASSWORD=devcraftPwd
mvn spring-boot:run
```

The API starts on `http://localhost:7490`, and Flyway loads three readers, five books, six favourites and three reviews. Start with `--spring.jpa.show-sql=true` to see the SQL Hibernate sends.

## Test

```bash
mvn test
```

The tests run on H2 and record every SQL statement, to check what Hibernate actually does.

## License

MIT (c) CS Solution
