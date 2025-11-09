package com.chanuth.hotel.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.Date;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Table(name = "reservations")
@Entity
@Data
@Accessors(chain = true)
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(length = 20, unique = true, nullable = false)
  private String code;

  @Column(nullable = false)
  private int bagsCount;

  @Column(nullable = false)
  private Date departureDate;

  @Column(nullable = false)
  private Date arrivalDate;

  @Column(nullable = false)
  private int roomNumber;

  @Column(nullable = false)
  private String[] extras;

  @ManyToOne(targetEntity = User.class, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  @JsonBackReference(value = "user-reservations")
  private User user;

  @Lob
  @Column
  private String note;
}
