package io.chanuth.starter.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok names the boolean getter isPublic() and the setter setPublic(), which Jackson reads as a property
// called "public". Ignore that name so only the explicit "isPublic" exists, in the JSON and in the API docs.
@JsonIgnoreProperties({"public"})
@Table(name = "projects", indexes = {@Index(name = "idx_title", columnList = "title")})
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    private Long id;

    @Column(length = 120, unique = true)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatusEnum status = ProjectStatusEnum.PLANNING;

    @JsonProperty("isPublic")
    @Column(name = "is_public", columnDefinition = "tinyint(1) not null default 0")
    private boolean isPublic;

    @Column(name = "max_members")
    private int maxMembers;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updatedAt;
}
