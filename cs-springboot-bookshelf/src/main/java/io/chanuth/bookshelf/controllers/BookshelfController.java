package io.chanuth.bookshelf.controllers;

import io.chanuth.bookshelf.dtos.Dtos.BookReviewsResponse;
import io.chanuth.bookshelf.dtos.Dtos.BookSummary;
import io.chanuth.bookshelf.dtos.Dtos.ReaderSummary;
import io.chanuth.bookshelf.dtos.Dtos.ReaderWithFavourites;
import io.chanuth.bookshelf.dtos.Dtos.ReviewRequest;
import io.chanuth.bookshelf.dtos.Dtos.ReviewResponse;
import io.chanuth.bookshelf.services.BookshelfService;
import io.chanuth.bookshelf.services.BookshelfService.SavedReview;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookshelfController {

    private final BookshelfService service;

    public BookshelfController(BookshelfService service) {
        this.service = service;
    }

    @GetMapping("/books")
    public List<BookSummary> books() {
        return service.books();
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        service.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Part 1: favourites ----------

    @GetMapping("/readers")
    public List<ReaderWithFavourites> readers() {
        return service.readersWithFavourites();
    }

    @GetMapping("/readers/{readerId}/favourites")
    public List<BookSummary> favourites(@PathVariable Long readerId) {
        return service.favouritesOf(readerId);
    }

    /** PUT, because doing it twice has the same effect as doing it once: 201 the first time, 200 after. */
    @PutMapping("/readers/{readerId}/favourites/{bookId}")
    public ResponseEntity<Void> addFavourite(@PathVariable Long readerId, @PathVariable Long bookId) {
        boolean added = service.addFavourite(readerId, bookId);
        return ResponseEntity.status(added ? HttpStatus.CREATED : HttpStatus.OK).build();
    }

    @DeleteMapping("/readers/{readerId}/favourites/{bookId}")
    public ResponseEntity<Void> removeFavourite(@PathVariable Long readerId, @PathVariable Long bookId) {
        service.removeFavourite(readerId, bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}/fans")
    public List<ReaderSummary> fans(@PathVariable Long bookId) {
        return service.fansOf(bookId);
    }

    // ---------- Part 2: reviews ----------

    @PutMapping("/readers/{readerId}/reviews/{bookId}")
    public ResponseEntity<ReviewResponse> saveReview(@PathVariable Long readerId, @PathVariable Long bookId,
                                                     @Valid @RequestBody ReviewRequest request) {
        SavedReview saved = service.saveReview(readerId, bookId, request);
        return ResponseEntity.status(saved.created() ? HttpStatus.CREATED : HttpStatus.OK).body(saved.review());
    }

    @GetMapping("/books/{bookId}/reviews")
    public BookReviewsResponse reviewsOfBook(@PathVariable Long bookId) {
        return service.reviewsOf(bookId);
    }

    @GetMapping("/readers/{readerId}/reviews")
    public List<ReviewResponse> reviewsByReader(@PathVariable Long readerId) {
        return service.reviewsBy(readerId);
    }

    @DeleteMapping("/readers/{readerId}/reviews/{bookId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long readerId, @PathVariable Long bookId) {
        service.deleteReview(readerId, bookId);
        return ResponseEntity.noContent().build();
    }
}
