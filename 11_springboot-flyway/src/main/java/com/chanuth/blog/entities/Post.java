package com.chanuth.blog.entities;

import java.util.Date;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Table(name = "posts")
@Entity
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(unique = true, length = 200, nullable = false)
  private String title;

  @Column(columnDefinition = "LONGTEXT")
  private String description;

  @Column(nullable = false, columnDefinition = "varchar(10) not null default 'PENDING'")
  @Enumerated(EnumType.STRING)
  private PostStatusEnum status;

  @ManyToOne(targetEntity = Author.class, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "author_id", nullable = false, referencedColumnName = "id")
  private Author author;

  @CreationTimestamp
  @Column(updatable = false)
  protected Date createdAt;

  @UpdateTimestamp
  protected Date updatedAt;
}
