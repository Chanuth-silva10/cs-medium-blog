package io.chanuth.bookshelf.listdemo;

import io.chanuth.bookshelf.entities.Book;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-only: the same readers table and join table as Reader, but with a List instead of a Set.
 * It exists to show what Hibernate does differently with a List.
 */
@Entity
@Table(name = "readers")
public class ListReader {

    @Id
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "reader_favourite_books",
            joinColumns = @JoinColumn(name = "reader_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<Book> favouriteBooks = new ArrayList<>();

    public List<Book> getFavouriteBooks() {
        return favouriteBooks;
    }
}
