package io.chanuth.bookshelf.repositories;

import io.chanuth.bookshelf.dtos.RatingSummary;
import io.chanuth.bookshelf.entities.BookReview;
import io.chanuth.bookshelf.entities.BookReviewId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** The repository's id type is the composite key class. */
public interface BookReviewRepository extends JpaRepository<BookReview, BookReviewId> {

    @Query("""
            select r from BookReview r join fetch r.reader
            where r.book.id = :bookId
            order by r.updatedAt desc
            """)
    List<BookReview> findByBookWithReader(@Param("bookId") Long bookId);

    @Query("""
            select r from BookReview r join fetch r.book
            where r.reader.id = :readerId
            order by r.updatedAt desc
            """)
    List<BookReview> findByReaderWithBook(@Param("readerId") Long readerId);

    /** The database works out the average and the count, instead of loading every review into memory. */
    @Query("""
            select new io.chanuth.bookshelf.dtos.RatingSummary(avg(r.rating), count(r))
            from BookReview r where r.book.id = :bookId
            """)
    RatingSummary ratingSummary(@Param("bookId") Long bookId);
}
