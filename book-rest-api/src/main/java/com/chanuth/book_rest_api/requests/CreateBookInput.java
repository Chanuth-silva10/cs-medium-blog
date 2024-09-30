package com.chanuth.book_rest_api.requests;

import com.chanuth.book_rest_api.entities.Book;
import com.chanuth.book_rest_api.entities.BookGenreEnum;

import java.util.Date;

public record CreateBookInput(String title, String author, BookGenreEnum genre, Date publicationDate) {
    public Book toBook() {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPublicationDate(publicationDate);
        return book;
    }
}
