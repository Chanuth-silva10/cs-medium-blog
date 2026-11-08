-- Sample data lives in a migration, so it is inserted exactly once per database.
-- On a fresh database the generated ids follow the insert order: readers 1-3, books 1-5.
INSERT INTO readers (email, display_name)
VALUES ('amara@devcraft.io', 'Amara'),
       ('kenji@devcraft.io', 'Kenji'),
       ('lucia@devcraft.io', 'Lucia');

INSERT INTO books (title, author, published_year, description)
VALUES ('The Pragmatic Programmer', 'David Thomas, Andrew Hunt', 2019, 'Practical advice for working programmers'),
       ('Clean Code', 'Robert C. Martin', 2008, 'Writing code that other people can read'),
       ('Designing Data-Intensive Applications', 'Martin Kleppmann', 2017, 'How data systems really work'),
       ('Refactoring', 'Martin Fowler', 2018, 'Improving the design of existing code'),
       ('Domain-Driven Design', 'Eric Evans', 2003, 'Tackling complexity in the heart of software');

INSERT INTO reader_favourite_books (reader_id, book_id)
VALUES (1, 1), (1, 3), (2, 1), (2, 2), (2, 4), (3, 3);

INSERT INTO book_reviews (reader_id, book_id, rating, comment, created_at, updated_at)
VALUES (1, 1, 5, 'Still the best first book for new developers', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
       (2, 1, 4, 'Great habits, a few chapters feel dated', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)),
       (3, 3, 5, 'Changed how I think about databases', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6));
