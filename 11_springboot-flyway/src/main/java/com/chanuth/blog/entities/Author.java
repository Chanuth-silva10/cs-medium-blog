package com.chanuth.blog.entities;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Table(name = "authors")
@Entity
public class Author {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(unique = true, length = 100, nullable = false)
  private String name;

  @Column(unique = true, length = 100, nullable = false)
  private String email;

  @Column(length = 100, nullable = false)
  private String password;

  @Column(nullable = false, columnDefinition = "tinyint not null default 0")
  private boolean enabled;

  @CreationTimestamp
  @Column(updatable = false)
  protected Date createdAt;

  @UpdateTimestamp
  protected Date updatedAt;
}
