package io.chanuth.bookshelf.repositories;

import io.chanuth.bookshelf.entities.Reader;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReaderRepository extends JpaRepository<Reader, Long> {

    /**
     * Loads the readers AND their favourite books in a single query. Without the entity graph,
     * reading each reader's favourites later costs one extra query per reader (the N+1 problem).
     */
    @EntityGraph(attributePaths = "favouriteBooks")
    @Query("select r from Reader r order by r.id")
    List<Reader> findAllWithFavourites();
}
