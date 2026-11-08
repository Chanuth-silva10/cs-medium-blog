package io.chanuth.bookshelf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.bookshelf.entities.BookReview;
import io.chanuth.bookshelf.entities.BookReviewId;
import io.chanuth.bookshelf.repositories.BookRepository;
import io.chanuth.bookshelf.repositories.BookReviewRepository;
import io.chanuth.bookshelf.repositories.ReaderRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Chapter 22: a many-to-many with extra columns. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReviewsTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ReaderRepository readerRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookReviewRepository reviewRepository;

    @Autowired
    TransactionTemplate tx;

    private int reviewRows(long readerId, long bookId) {
        return jdbc.queryForObject("select count(*) from book_reviews where reader_id = ? and book_id = ?",
                Integer.class, readerId, bookId);
    }

    @Test
    void createsAReviewThenUpdatesItInPlace() throws Exception {
        String created = mvc.perform(put("/readers/3/reviews/2").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3,\"comment\":\"Useful, but strict\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.readerName").value("Lucia"))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.rating").value(3))
                .andReturn().getResponse().getContentAsString();

        Thread.sleep(20);
        String updated = mvc.perform(put("/readers/3/reviews/2").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Better on a second read\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Better on a second read"))
                .andReturn().getResponse().getContentAsString();

        assertThat(reviewRows(3, 2)).isEqualTo(1);
        assertThat(field(updated, "createdAt")).isEqualTo(field(created, "createdAt"));
        assertThat(field(updated, "updatedAt")).isNotEqualTo(field(created, "updatedAt"));
    }

    @Test
    void mapsIdFillsTheCompositeKeyAndSaveInsertsWithoutAnExtraSelect() {
        SqlRecorder.clear();
        tx.executeWithoutResult((status) -> {
            var review = new BookReview(readerRepository.getReferenceById(1L), bookRepository.getReferenceById(4L), 4, null);
            BookReview saved = reviewRepository.saveAndFlush(review);
            // We never set the id ourselves, and save() kept working on OUR object.
            assertThat(saved).isSameAs(review);
            assertThat(review.getId()).isEqualTo(new BookReviewId(1L, 4L));
        });
        assertThat(reviewRows(1, 4)).isEqualTo(1);
        // Insert straight away: no "does this row exist?" select on book_reviews first.
        assertThat(SqlRecorder.statements().stream()
                .filter((s) -> s.startsWith("select") && s.contains("book_reviews"))).isEmpty();
    }
    @Test
    void aBookPageShowsTheAverageTheCountAndTheReviews() throws Exception {
        mvc.perform(get("/books/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.reviews.length()").value(2));

        mvc.perform(put("/readers/3/reviews/1").contentType(MediaType.APPLICATION_JSON).content("{\"rating\":3}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/books/1/reviews"))
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.reviewCount").value(3));
    }

    @Test
    void aBookWithoutReviewsHasNoAverage() throws Exception {
        mvc.perform(get("/books/5/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").doesNotExist())
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.reviews.length()").value(0));
    }

    @Test
    void listsAReadersReviews() throws Exception {
        mvc.perform(get("/readers/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookTitle").value("The Pragmatic Programmer"));
    }

    @Test
    void validatesTheRating() throws Exception {
        mvc.perform(put("/readers/1/reviews/2").contentType(MediaType.APPLICATION_JSON).content("{\"rating\":6}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.rating").exists());
        mvc.perform(put("/readers/1/reviews/2").contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"no rating\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void theDatabaseRejectsAnOutOfRangeRatingToo() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into book_reviews (reader_id, book_id, rating, created_at, updated_at) values (1, 2, 9, now(), now())"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletesAReview() throws Exception {
        mvc.perform(delete("/readers/2/reviews/1")).andExpect(status().isNoContent());
        mvc.perform(delete("/readers/2/reviews/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reader 2 has not reviewed book 1"));
    }

    @Test
    void deletingABookDeletesItsReviews() throws Exception {
        mvc.perform(delete("/books/3")).andExpect(status().isNoContent());

        mvc.perform(get("/readers/3/reviews")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/books/3/reviews")).andExpect(status().isNotFound());
    }

    private static Object field(String json, String name) {
        return new tools.jackson.databind.json.JsonMapper().readValue(json, Map.class).get(name);
    }
}
