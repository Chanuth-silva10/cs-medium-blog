package com.chanuth.movie.repositories;

import com.chanuth.movie.entities.UserMovie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMovieRepository extends CrudRepository<UserMovie, UserMovie.UserMovieId> {

}
