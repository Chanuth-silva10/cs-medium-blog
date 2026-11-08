package io.chanuth.bookshelf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

/**
 * Part 2: when a link between two entities carries its own data (a rating, a comment, a date),
 * the join table becomes an entity of its own. The many-to-many turns into two many-to-ones.
 *
 * Persistable fixes a trap of composite keys: our id is never null, so Spring Data's save() would
 * assume the row already exists, run a SELECT to check, and merge into a NEW object, leaving ours
 * untouched. isNew() tells it the truth instead.
 */
@Table(name = "book_reviews")
@Entity
public class BookReview implements Persistable<BookReviewId> {

    @EmbeddedId
    private BookReviewId id = new BookReviewId();

    // @MapsId says: the readerId part of the primary key IS this association's id.
    // Set the reader and Hibernate fills the key for us, so the id is never typed in twice.
    @MapsId("readerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id")
    private Reader reader;

    @MapsId("bookId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BookReview() {
        // Required by JPA.
    }

    public BookReview(Reader reader, Book book, int rating, String comment) {
        this.reader = reader;
        this.book = book;
        this.rating = rating;
        this.comment = comment;
    }

    public void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    /** createdAt is filled when the row is inserted, so a review without it has never been saved. */
    @Override
    public boolean isNew() {
        return createdAt == null;
    }

    @Override
    public BookReviewId getId() {
        return id;
    }

    public Reader getReader() {
        return reader;
    }

    public Book getBook() {
        return book;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
