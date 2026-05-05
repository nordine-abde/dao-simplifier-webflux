package anordine.dao.simplifier.webflux.entity;

import java.util.UUID;

/**
 * Base soft-delete entity with UUID identifiers.
 */
public abstract class SoftDeleteUuidEntity extends SoftDeleteEntity<UUID> {

    /**
     * Creates a UUID soft-delete entity.
     */
    public SoftDeleteUuidEntity() {
    }

    /**
     * Generates a random UUID for new soft-delete entities.
     *
     * @return generated UUID
     */
    @Override
    protected UUID generateId() {
        return UUID.randomUUID();
    }
}
