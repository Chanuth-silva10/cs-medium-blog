package com.chanuth.hotel.models.dtos;

import com.chanuth.hotel.models.Reservation;
import java.util.Date;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateReservationDto {
  @NotNull(message = "The number of bags is required.")
  @Min(value = 1, message = "The number of bags must be greater than 0")
  @Max(value = 3, message = "The number of bags must be greater than 3")
  private int bagsCount;

  @NotNull(message = "The departure date is required.")
  @FutureOrPresent(message = "The departure date must be today or in the future.")
  private Date departureDate;

  @NotNull(message = "The arrival date is required.")
  @FutureOrPresent(message = "The arrival date must be today or in the future.")
  private Date arrivalDate;

  @NotNull(message = "The room's number is required.")
  @Positive(message = "The room's number must be greater than 0")
  private int roomNumber;

  @NotNull(message = "The extras is required.")
  @NotEmpty(message = "The extras must have at least one item.")
  private String[] extras;

  @NotNull(message = "The user's Id is required.")
  @Positive(message = "The user's Id must be greater than 0")
  private int userId;

  private String note;

  public Reservation toReservation() {
    return new Reservation()
        .setBagsCount(bagsCount)
        .setDepartureDate(departureDate)
        .setArrivalDate(arrivalDate)
        .setRoomNumber(roomNumber)
        .setExtras(extras)
        .setNote(note);
  }
}
