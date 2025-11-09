package com.chanuth.hotel.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Table(name = "users")
@Entity
@Data
@Accessors(chain = true)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(length = 100, nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private Date birthDate;

  @Column(length = 10, nullable = false)
  private String gender;

  @CreationTimestamp
  @Column(updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  private Date updatedAt;

  @OneToOne(cascade = CascadeType.REMOVE)
  @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
  private Address address;

  @OneToMany(targetEntity = Reservation.class, mappedBy = "user")
  @JsonManagedReference(value = "user-reservations")
  private List<Reservation> reservations;
}
