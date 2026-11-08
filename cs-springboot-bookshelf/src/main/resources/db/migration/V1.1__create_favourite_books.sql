-- Part 1: the join table of a plain many-to-many. It holds nothing but the two ids.
-- The composite primary key means a reader can favourite the same book only once.
CREATE TABLE reader_favourite_books
(
    reader_id BIGINT NOT NULL,
    book_id   BIGINT NOT NULL,
    CONSTRAINT pk_reader_favourite_books PRIMARY KEY (reader_id, book_id),
    CONSTRAINT fk_favourite_reader FOREIGN KEY (reader_id) REFERENCES readers (id) ON DELETE CASCADE,
    CONSTRAINT fk_favourite_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);
