package io.chanuth.clubs.model;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
 * What every document has in common: an ObjectId and two timestamps. Spring Data auditing fills
 * the timestamps: createdAt once, when the document is first saved, updatedAt on every save.
 */
public abstract class BaseDocument {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
