package com.chanuth.book_rest_api.controllers;

import com.chanuth.book_rest_api.entities.Book;
import com.chanuth.book_rest_api.requests.CreateBookInput;
import com.chanuth.book_rest_api.requests.UpdateBookInput;
import com.chanuth.book_rest_api.services.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getBooks() {
        return bookService.findAll();
    }

    @PostMapping
    public Book createBook(@RequestBody CreateBookInput input) {
        return bookService.create(input.toBook());
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable int id) {
        return bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable int id, @RequestBody UpdateBookInput input) {
        return bookService.findById(id).map(book -> {
            book.setGenre(input.genre());
            book.setPublicationDate(input.publicationDate());
            return bookService.update(book);
        }).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable int id) {
        bookService.delete(id);
    }
}
