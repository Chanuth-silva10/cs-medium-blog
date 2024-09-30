package com.chanuth.book_rest_api.requests;

import com.chanuth.book_rest_api.entities.BookGenreEnum;

import java.util.Date;

public record UpdateBookInput(BookGenreEnum genre, Date publicationDate) {
}
