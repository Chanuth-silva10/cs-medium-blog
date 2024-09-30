package com.chanuth.book_rest_api.repositories;

import com.chanuth.book_rest_api.entities.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CrudRepository<Book, Integer> {
}

