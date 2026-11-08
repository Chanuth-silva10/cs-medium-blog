package io.chanuth.bookshelf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.bookshelf.entities.Reader;
import io.chanuth.bookshelf.listdemo.ListReader;
import io.chanuth.bookshelf.repositories.ReaderRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Chapter 21: the plain many-to-many. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FavouritesTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ReaderRepository readerRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TransactionTemplate tx;

    private int favouriteRows(long readerId, long bookId) {
        return jdbc.queryForObject("select count(*) from reader_favourite_books where reader_id = ? and book_id = ?",
                Integer.class, readerId, bookId);
    }

    @Test
    void listsReadersWithTheirFavourites() throws Exception {
        mvc.perform(get("/readers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Amara"))
                .andExpect(jsonPath("$[0].favouriteTitles[0]").value("Designing Data-Intensive Applications"))
                .andExpect(jsonPath("$[0].favouriteTitles[1]").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$[1].favouriteTitles.length()").value(3));
    }

    @Test
    void theInverseSideAnswersWhoLikesABook() throws Exception {
        mvc.perform(get("/books/1/fans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].displayName").value("Amara"))
                .andExpect(jsonPath("$[1].displayName").value("Kenji"));
    }

    @Test
    void addingAFavouriteTwiceKeepsOneRow() throws Exception {
        mvc.perform(put("/readers/3/favourites/5")).andExpect(status().isCreated());
        mvc.perform(put("/readers/3/favourites/5")).andExpect(status().isOk());

        assertThat(favouriteRows(3, 5)).isEqualTo(1);
        mvc.perform(get("/readers/3/favourites")).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void withASetRemovingOneFavouriteDeletesExactlyOneRow() throws Exception {
        SqlRecorder.clear();
        mvc.perform(delete("/readers/2/favourites/2")).andExpect(status().isNoContent());

        List<String> deletes = SqlRecorder.statements().stream().filter((s) -> s.startsWith("delete")).toList();
        assertThat(deletes).hasSize(1);
        assertThat(deletes.getFirst()).contains("reader_id=?").contains("book_id=?");
        assertThat(SqlRecorder.count("insert")).isZero();
        assertThat(favouriteRows(2, 1)).isEqualTo(1);
        assertThat(favouriteRows(2, 4)).isEqualTo(1);
    }

    @Test
    void withAListRemovingOneFavouriteRewritesTheWholeCollection() {
        SqlRecorder.clear();
        tx.executeWithoutResult((status) -> {
            ListReader kenji = entityManager.find(ListReader.class, 2L);
            kenji.getFavouriteBooks().removeIf((book) -> book.getId() == 2L);
            entityManager.flush();
        });

        List<String> writes = SqlRecorder.statements().stream()
                .filter((s) -> s.startsWith("delete") || s.startsWith("insert")).toList();
        // One delete of EVERY row for this reader, then one insert per book that should stay.
        assertThat(writes.getFirst()).startsWith("delete").doesNotContain("book_id");
        assertThat(SqlRecorder.count("insert")).isEqualTo(2);
    }

    @Test
    void loadingFavouritesOneReaderAtATimeIsNPlusOne() {
        SqlRecorder.clear();
        tx.executeWithoutResult((status) ->
                readerRepository.findAll().forEach((reader) -> reader.getFavouriteBooks().size()));
        long naive = SqlRecorder.count("select");

        SqlRecorder.clear();
        tx.executeWithoutResult((status) ->
                readerRepository.findAllWithFavourites().forEach((reader) -> reader.getFavouriteBooks().size()));
        long withGraph = SqlRecorder.count("select");

        assertThat(naive).isEqualTo(4);   // 1 for the readers + 1 per reader (3 readers)
        assertThat(withGraph).isEqualTo(1);
    }

    @Test
    void lazyCollectionsCannotBeReadAfterTheTransactionEnds() {
        Reader amara = readerRepository.findById(1L).orElseThrow();

        assertThatThrownBy(() -> amara.getFavouriteBooks().size()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void unknownReaderOrBookAnswers404() throws Exception {
        mvc.perform(get("/readers/99/favourites"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No reader with id 99"));
        mvc.perform(put("/readers/1/favourites/99")).andExpect(status().isNotFound());
    }

    @Test
    void deletingABookRemovesItFromEveryonesFavourites() throws Exception {
        mvc.perform(delete("/books/3")).andExpect(status().isNoContent());

        mvc.perform(get("/readers/1/favourites"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("The Pragmatic Programmer"));
        assertThat(jdbc.queryForObject("select count(*) from books", Integer.class)).isEqualTo(4);
    }
}
