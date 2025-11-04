package com.chanuth.movie.entities;

import javax.persistence.*;

@Table(name = "users")
@Entity
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false)
  private Integer id;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Column(unique = true, nullable = false, length = 100)
  private String name;

  public User() {}

  public User(String email, String name) {
    this.email = email;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }
}
