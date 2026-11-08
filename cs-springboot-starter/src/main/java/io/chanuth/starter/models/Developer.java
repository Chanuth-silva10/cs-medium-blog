package io.chanuth.starter.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Table(name = "developers")
@Entity
public class Developer {

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

    @Column(name = "github_url", length = 200)
    private String githubUrl;

    @Column(nullable = false, columnDefinition = "tinyint(1) not null default 0")
    private boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    protected Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    protected Date updatedAt;
}
