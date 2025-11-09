package com.chanuth.theator.repositories;

import com.chanuth.theator.entities.UserMovie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMovieRepository extends CrudRepository<UserMovie, UserMovie.UserMovieId> {

}
