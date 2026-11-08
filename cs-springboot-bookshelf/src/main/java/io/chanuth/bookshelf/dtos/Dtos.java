package io.chanuth.bookshelf.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.chanuth.bookshelf.entities.Book;
import io.chanuth.bookshelf.entities.BookReview;
import io.chanuth.bookshelf.entities.Reader;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * The shapes the API reads and writes. Returning entities instead would be a trap: Reader points to
 * Book, Book points back to Reader, and turning that into JSON never ends.
 */
public final class Dtos {

    private Dtos() {
    }

    public record BookSummary(Long id, String title, String author, int publishedYear) {

        public static BookSummary from(Book book) {
            return new BookSummary(book.getId(), book.getTitle(), book.getAuthor(), book.getPublishedYear());
        }
    }

    public record ReaderSummary(Long id, String displayName) {

        public static ReaderSummary from(Reader reader) {
            return new ReaderSummary(reader.getId(), reader.getDisplayName());
        }
    }

    public record ReaderWithFavourites(Long id, String displayName, List<String> favouriteTitles) {

        public static ReaderWithFavourites from(Reader reader) {
            List<String> titles = reader.getFavouriteBooks().stream()
                    .map(Book::getTitle)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            return new ReaderWithFavourites(reader.getId(), reader.getDisplayName(), titles);
        }
    }

    public record ReviewRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 2000) String comment) {
    }

    public record ReviewResponse(Long readerId, String readerName, Long bookId, String bookTitle,
                                 int rating, String comment, Instant createdAt, Instant updatedAt) {

        public static ReviewResponse from(BookReview review) {
            return new ReviewResponse(review.getReader().getId(), review.getReader().getDisplayName(),
                    review.getBook().getId(), review.getBook().getTitle(), review.getRating(),
                    review.getComment(), review.getCreatedAt(), review.getUpdatedAt());
        }
    }

    /** What a book page needs: the average, the count and the reviews themselves. */
    public record BookReviewsResponse(Long bookId, String title, Double averageRating, long reviewCount,
                                      List<ReviewResponse> reviews) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(String message, java.util.Map<String, String> errors) {

        public static ErrorResponse of(String message) {
            return new ErrorResponse(message, null);
        }
    }
}
