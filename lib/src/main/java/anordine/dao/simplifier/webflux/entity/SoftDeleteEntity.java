package anordine.dao.simplifier.webflux.entity;

import java.time.Instant;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Base entity for rows that are hidden by DAO-owned reads instead of being
 * physically deleted.
 *
 * @param <ID> entity identifier type
 */
public abstract class SoftDeleteEntity<ID> extends BaseEntity<ID> {

    @Column("deleted")
    protected boolean deleted;

    @Column("deleted_at")
    protected Instant deletedAt;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
