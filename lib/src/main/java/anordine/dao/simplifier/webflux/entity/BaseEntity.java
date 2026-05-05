package anordine.dao.simplifier.webflux.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Base entity for DAO-managed lifecycle timestamps and Spring Data R2DBC
 * insert/update detection.
 *
 * @param <ID> entity identifier type
 */
public abstract class BaseEntity<ID> implements Persistable<ID> {

    @Id
    @Column("id")
    protected ID id;

    @Column("created_at")
    protected Instant createdAt;

    @Column("updated_at")
    protected Instant updatedAt;

    @Transient
    protected boolean isNew;

    @Override
    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void prePersist() {
        prePersist(false);
    }

    public void prePersist(boolean asNewWithId) {
        if (asNewWithId) {
            if (id == null) {
                throw new IllegalArgumentException("Cannot force insert with an assigned id when id is null");
            }
            isNew = true;
        } else if (id == null) {
            id = generateId();
            if (id == null) {
                throw new IllegalStateException("Generated id must not be null");
            }
            isNew = true;
        }

        Instant now = Instant.now();
        if (isNew) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public void markAsNotNew() {
        this.isNew = false;
    }

    protected abstract ID generateId();
}
