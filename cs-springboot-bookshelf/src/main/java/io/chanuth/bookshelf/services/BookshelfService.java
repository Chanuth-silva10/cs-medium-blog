package io.chanuth.bookshelf.services;

import io.chanuth.bookshelf.dtos.Dtos.BookReviewsResponse;
import io.chanuth.bookshelf.dtos.Dtos.BookSummary;
import io.chanuth.bookshelf.dtos.Dtos.ReaderSummary;
import io.chanuth.bookshelf.dtos.Dtos.ReaderWithFavourites;
import io.chanuth.bookshelf.dtos.Dtos.ReviewRequest;
import io.chanuth.bookshelf.dtos.Dtos.ReviewResponse;
import io.chanuth.bookshelf.dtos.RatingSummary;
import io.chanuth.bookshelf.entities.Book;
import io.chanuth.bookshelf.entities.BookReview;
import io.chanuth.bookshelf.entities.BookReviewId;
import io.chanuth.bookshelf.entities.Reader;
import io.chanuth.bookshelf.exceptions.NotFoundException;
import io.chanuth.bookshelf.repositories.BookRepository;
import io.chanuth.bookshelf.repositories.BookReviewRepository;
import io.chanuth.bookshelf.repositories.ReaderRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Every method runs in a transaction. The many-to-many collections are LAZY, so they can only be read
 * while the database session is open, that is, inside these methods.
 */
@Service
@Transactional(readOnly = true)
public class BookshelfService {

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final BookReviewRepository reviewRepository;

    public BookshelfService(ReaderRepository readerRepository, BookRepository bookRepository,
                            BookReviewRepository reviewRepository) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    // ---------- Part 1: favourite books (a plain many-to-many) ----------

    public List<BookSummary> books() {
        return bookRepository.findAllByOrderByIdAsc().stream().map(BookSummary::from).toList();
    }

    /** One query for all readers and their favourites, thanks to the entity graph. */
    public List<ReaderWithFavourites> readersWithFavourites() {
        return readerRepository.findAllWithFavourites().stream().map(ReaderWithFavourites::from).toList();
    }

    public List<BookSummary> favouritesOf(Long readerId) {
        return reader(readerId).getFavouriteBooks().stream()
                .sorted(Comparator.comparing(Book::getId))
                .map(BookSummary::from)
                .toList();
    }

    public List<ReaderSummary> fansOf(Long bookId) {
        return book(bookId).getFavouredBy().stream()
                .sorted(Comparator.comparing(Reader::getId))
                .map(ReaderSummary::from)
                .toList();
    }

    /** @return true if the book was newly added, false if it already was a favourite */
    @Transactional
    public boolean addFavourite(Long readerId, Long bookId) {
        // No save() call needed: the reader is managed, so Hibernate writes the change at commit.
        return reader(readerId).addFavourite(book(bookId));
    }

    @Transactional
    public void removeFavourite(Long readerId, Long bookId) {
        reader(readerId).removeFavourite(book(bookId));
    }

    // ---------- Part 2: reviews (a many-to-many with extra columns) ----------

    /** Creates the review, or updates it if this reader already reviewed this book. */
    @Transactional
    public SavedReview saveReview(Long readerId, Long bookId, ReviewRequest request) {
        Reader reader = reader(readerId);
        Book book = book(bookId);

        return reviewRepository.findById(new BookReviewId(readerId, bookId))
                .map((existing) -> {
                    existing.update(request.rating(), request.comment());
                    // Write now, so the response carries the new updatedAt value.
                    reviewRepository.flush();
                    return new SavedReview(ReviewResponse.from(existing), false);
                })
                .orElseGet(() -> {
                    BookReview created = reviewRepository.saveAndFlush(
                            new BookReview(reader, book, request.rating(), request.comment()));
                    return new SavedReview(ReviewResponse.from(created), true);
                });
    }

    public BookReviewsResponse reviewsOf(Long bookId) {
        Book book = book(bookId);
        RatingSummary summary = reviewRepository.ratingSummary(bookId);
        List<ReviewResponse> reviews = reviewRepository.findByBookWithReader(bookId).stream()
                .map(ReviewResponse::from).toList();
        Double average = summary.average() == null ? null : Math.round(summary.average() * 10) / 10.0;
        return new BookReviewsResponse(book.getId(), book.getTitle(), average, summary.count(), reviews);
    }

    public List<ReviewResponse> reviewsBy(Long readerId) {
        reader(readerId);
        return reviewRepository.findByReaderWithBook(readerId).stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public void deleteReview(Long readerId, Long bookId) {
        BookReviewId id = new BookReviewId(readerId, bookId);
        if (!reviewRepository.existsById(id)) {
            throw new NotFoundException("Reader " + readerId + " has not reviewed book " + bookId);
        }
        reviewRepository.deleteById(id);
    }

    /** The database's ON DELETE CASCADE removes the book's favourites and reviews with it. */
    @Transactional
    public void deleteBook(Long bookId) {
        bookRepository.delete(book(bookId));
    }

    // ---------- helpers ----------

    private Reader reader(Long id) {
        return readerRepository.findById(id).orElseThrow(() -> new NotFoundException("No reader with id " + id));
    }

    private Book book(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new NotFoundException("No book with id " + id));
    }

    public record SavedReview(ReviewResponse review, boolean created) {
    }
}
