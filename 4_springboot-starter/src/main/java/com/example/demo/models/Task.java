package com.example.demo.models;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;



@Table(name = "tasks", indexes = {
    @Index(name = "idx_title", columnList = "title")
})
@Entity()
@NoArgsConstructor()
@Accessors(chain = true)
@Data()
public class Task {
  @Id()
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(insertable = false, updatable = false)
  private int id;

  @Column(length = 100, unique = true)
  private String title;

  @Lob()
  private String description;

  @Enumerated(EnumType.STRING) // Store enum as string instead of number
  @Column(length = 20)
  private TaskStatusEnum status = TaskStatusEnum.PENDING;

  @Column(name = "max_duration")
  private int maxDuration;

  @Column(name = "is_public", columnDefinition = "boolean default false")
  private boolean isPublic;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Date updatedAt;
}
