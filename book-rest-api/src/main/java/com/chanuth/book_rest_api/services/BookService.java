package com.chanuth.book_rest_api.services;

import com.chanuth.book_rest_api.entities.Book;
import com.chanuth.book_rest_api.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book create(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        bookRepository.findAll().forEach(books::add);
        return books;
    }

    public Optional<Book> findById(int id) {
        return bookRepository.findById(id);
    }

    public Book update(Book book) {
        return bookRepository.save(book);
    }

    public void delete(int id) {
        bookRepository.deleteById(id);
    }
}

