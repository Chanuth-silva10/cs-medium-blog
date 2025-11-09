package com.chanuth.hotel.repositories;

import com.chanuth.hotel.models.Reservation;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, Integer> {
  Optional<Reservation> findFirstByOrderByIdDesc();

  Optional<Reservation> findByCode(String code);
}

