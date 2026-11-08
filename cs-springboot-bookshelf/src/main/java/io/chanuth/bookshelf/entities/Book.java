package io.chanuth.bookshelf.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Table(name = "books")
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(name = "published_year", nullable = false)
    private int publishedYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The inverse side of the relationship. mappedBy names the field on Reader that owns it, so
     * Hibernate never writes the join table from here. It only lets us ask "who likes this book?".
     */
    @ManyToMany(mappedBy = "favouriteBooks")
    private Set<Reader> favouredBy = new HashSet<>();

    protected Book() {
        // Required by JPA.
    }

    public Book(String title, String author, int publishedYear, String description) {
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublishedYear() {
        return publishedYear;
    }

    public String getDescription() {
        return description;
    }

    public Set<Reader> getFavouredBy() {
        return favouredBy;
    }
}
