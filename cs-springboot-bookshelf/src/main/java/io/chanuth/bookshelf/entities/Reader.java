package io.chanuth.bookshelf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Table(name = "readers")
@Entity
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * Part 1: a plain many-to-many. Reader is the OWNING side: the @JoinTable lives here, and only
     * changes made to this set are written to the join table.
     *
     * No cascade on purpose. CascadeType.REMOVE here would delete the books themselves when a reader
     * is deleted, books that other readers still love.
     */
    @ManyToMany
    @JoinTable(
            name = "reader_favourite_books",
            joinColumns = @JoinColumn(name = "reader_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private Set<Book> favouriteBooks = new HashSet<>();

    protected Reader() {
        // Required by JPA.
    }

    public Reader(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    /**
     * Keeps BOTH sides in step. Hibernate only saves the owning side, but within the same request
     * code may read the other side too, and it should not see a different answer.
     *
     * @return false if the book was already a favourite
     */
    public boolean addFavourite(Book book) {
        boolean added = favouriteBooks.add(book);
        book.getFavouredBy().add(this);
        return added;
    }

    public boolean removeFavourite(Book book) {
        boolean removed = favouriteBooks.remove(book);
        book.getFavouredBy().remove(this);
        return removed;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Read-only view: changes must go through addFavourite and removeFavourite. */
    public Set<Book> getFavouriteBooks() {
        return Collections.unmodifiableSet(favouriteBooks);
    }
}
