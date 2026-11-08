-- Part 2: a many-to-many that carries its own data. The two ids are still the primary key,
-- and next to them sit the rating, the comment and the time of the review.
CREATE TABLE book_reviews
(
    reader_id  BIGINT      NOT NULL,
    book_id    BIGINT      NOT NULL,
    rating     INT         NOT NULL,
    comment    TEXT        NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    CONSTRAINT pk_book_reviews PRIMARY KEY (reader_id, book_id),
    CONSTRAINT ck_book_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_review_reader FOREIGN KEY (reader_id) REFERENCES readers (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- The primary key starts with reader_id, so "all reviews of a book" needs its own index.
CREATE INDEX idx_book_reviews_book ON book_reviews (book_id);
