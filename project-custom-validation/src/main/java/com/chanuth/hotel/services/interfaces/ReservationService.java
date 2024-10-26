package com.chanuth.hotel.services.interfaces;

import com.chanuth.hotel.models.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
  Reservation create(Reservation reservation);

  List<Reservation> findAll();

  Optional<Reservation> findByCode(String code);
}
