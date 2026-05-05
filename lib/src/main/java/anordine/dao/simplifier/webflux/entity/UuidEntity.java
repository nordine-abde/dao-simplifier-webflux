package anordine.dao.simplifier.webflux.entity;

import java.util.UUID;

/**
 * Base hard-delete entity with UUID identifiers.
 */
public abstract class UuidEntity extends BaseEntity<UUID> {

    /**
     * Creates a UUID hard-delete entity.
     */
    public UuidEntity() {
    }

    /**
     * Generates a random UUID for new hard-delete entities.
     *
     * @return generated UUID
     */
    @Override
    protected UUID generateId() {
        return UUID.randomUUID();
    }
}
