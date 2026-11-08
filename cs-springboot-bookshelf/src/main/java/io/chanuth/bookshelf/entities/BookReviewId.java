package io.chanuth.bookshelf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key of a review: one reader, one book. JPA requires a composite key class to be
 * Serializable and to define equals and hashCode based on its fields. Hibernate uses them to tell
 * whether two keys are the same row, for example when it looks an entity up in its cache.
 */
@Embeddable
public class BookReviewId implements Serializable {

    @Column(name = "reader_id")
    private Long readerId;

    @Column(name = "book_id")
    private Long bookId;

    protected BookReviewId() {
        // Required by JPA. With @MapsId, Hibernate fills both fields from the associations.
    }

    public BookReviewId(Long readerId, Long bookId) {
        this.readerId = readerId;
        this.bookId = bookId;
    }

    public Long getReaderId() {
        return readerId;
    }

    public Long getBookId() {
        return bookId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BookReviewId that)) {
            return false;
        }
        return Objects.equals(readerId, that.readerId) && Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readerId, bookId);
    }
}
